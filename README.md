# Urigym Backend

**우리짐(Urigym)** 백엔드입니다. 지도 기반 체육관 안내 서비스의 API 서버로, 일반 사용자 / 관장 / 관리자 3개 권한 체계를 제공합니다.

## 기술 스택

- Java 17, Spring Boot 3.4
- Spring Security + JWT (역할 기반 접근 제어)
- Spring Data JPA (조회는 파생 쿼리·Specification 우선, 집계만 `@Query`)
- PostgreSQL (운영) / H2 (로컬)
- Gradle

## 실행 방법

로컬은 H2 인메모리 DB와 시드 데이터를 사용합니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

### 로컬 시드 계정 (비밀번호 `urigym123`)

| 역할 | 이메일 |
|------|--------|
| 관리자 | admin@urigym.com |
| 관장 | owner@urigym.com |
| 일반 | user@urigym.com |

운영 환경에서는 `local` 프로필을 쓰지 않으므로 시드가 실행되지 않습니다.

## 권한 체계

`JwtAuthenticationFilter`가 토큰의 사용자 역할을 `ROLE_USER` / `ROLE_OWNER` / `ROLE_ADMIN` 권한으로 발급하고,
`@PreAuthorize`로 컨트롤러 단위에서 검사합니다. 관장 API는 여기에 더해 `GymService.getOwnedGym`으로
"본인이 등록한 체육관인지"를 서비스 계층에서 다시 확인합니다.

## 소셜 로그인 (카카오 / 네이버)

프론트엔드가 각 provider의 JS SDK로 로그인해 액세스 토큰을 발급받고, 그 토큰을
`POST /api/auth/oauth/{KAKAO|NAVER}`로 보내면 백엔드가 provider의 "내 정보 조회" API로
토큰을 검증한 뒤 우리 서비스의 JWT를 발급합니다. **provider의 클라이언트 시크릿은 필요 없습니다**
— 이미 provider가 검증해서 클라이언트에 내려준 토큰을 그대로 다시 검증하는 방식이라,
백엔드는 공개된 프로필 조회 엔드포인트만 호출하면 됩니다.

- 계정 연동: 소셜 로그인 이메일이 기존 비밀번호 계정과 같으면 자동으로 연동됩니다
  (provider가 이미 검증한 이메일이라는 전제). 한 사용자가 카카오·네이버를 모두 연동할 수도 있습니다
  (`UserOAuthAccount` 테이블, `(provider, provider_user_id)` 유니크).
- 이메일 동의를 하지 않은 소셜 로그인은 계정을 만들 수 없습니다(에러 메시지로 안내).
- `User.password`는 이제 nullable입니다 — 소셜 전용 계정은 비밀번호가 없고, 그런 계정으로
  이메일/비밀번호 로그인을 시도하면 "소셜 로그인으로 가입된 계정입니다" 안내를 반환합니다.
- Apple 로그인은 보류 — 별도 유료 Apple Developer Program 가입과 ES256 클라이언트 시크릿
  구현이 필요해서 `domain/oauth/OAuthProvider`에 추가만 해두면 됩니다.

## 주요 API

### 공개
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/gyms` | 체육관 목록 (정지된 체육관 자동 제외) |
| GET | `/api/gyms/ranked?limit=` | AI 랭킹 상위 체육관 |
| GET | `/api/gyms/search?keyword=` | 검색 |
| GET | `/api/gyms/location?minLat=...` | 지도 범위 조회 |
| GET | `/api/gyms/{id}/reviews` \| `/announcements` \| `/events` | 리뷰 / 공지 / 이벤트 |

### 인증 사용자
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/attendances` | 출석 체크 (`checkInMethod`: PHONE / QR) |
| GET | `/api/memberships/my-gyms` | 내가 등록된 체육관 |
| POST | `/api/reports` | 신고 / 문의 등록 |
| GET | `/api/notifications` | 알림 목록 |
| POST | `/api/owner-applications` | 관장 등록 신청 |
| POST | `/api/uploads` | 파일 업로드 |
| POST | `/api/auth/oauth/{KAKAO\|NAVER}` | 소셜 로그인 (없는 계정이면 자동 가입) |

### 관장 (`ROLE_OWNER`)
`/api/owner/gyms` 이하에서 체육관 CRUD, 관원 관리, 공지, 이벤트, 단체 메시지,
장기 미출석 관원 조회, 리뷰 열람(수정 불가), 출석 현황을 제공합니다.

### 관리자 (`ROLE_ADMIN`)
`/api/admin` 이하에서 사용자 관리(이름·전화·주소만 수정 가능), 관장 신청 승인/반려,
신고·문의 처리, 체육관 노출 정지/해제를 제공합니다.

## 설계 노트

### AI 랭킹
`GymRankingService`가 리뷰 품질(45%), 인기도(20%), 가격 경쟁력(20%), 신고 페널티(15%)를 가중 합산합니다.
리뷰 품질 산출은 `ReviewQualityScorer` 인터페이스로 분리되어 있고 현재는 휴리스틱 구현
(`HeuristicReviewQualityScorer`)이 기본입니다. LLM 기반 분석으로 바꾸려면 같은 인터페이스의
구현체를 `@Primary`로 추가하면 되고, 랭킹·컨트롤러 코드는 수정할 필요가 없습니다.

### 체육관 노출 정지
신고가 누적되면 랭킹 점수는 자동으로 낮아지지만, 목록에서 완전히 숨기는 것은
관리자가 기간을 지정해 수동으로 처리합니다(`PUT /api/admin/gyms/{id}/suspend?days=`).
공개 조회는 `GymSpecifications.visible()`을 통해 정지된 체육관을 걸러냅니다.

### 얼굴인식 / NFC 출석
스펙상 보류 단계이므로 `CheckInMethod`에 값과 분기는 존재하지만 실제 검증 로직은
`AttendanceService.verifyIdentity`에 주석으로 남겨두었고, 호출 시 501을 반환합니다.
현재 실제로 동작하는 방식은 전화번호 인증입니다.

### 파일 업로드
`FileStorageService`가 업로드 파일을 UUID 이름으로 `uploads/`에 저장하고 `/files/{이름}`으로 서빙합니다.
**주의**: 현재 `/files/**`는 인증 없이 접근 가능합니다(이름 추측만 어려운 상태). 사업자등록증 같은
민감 서류를 다루므로, 운영 배포 전에는 인증이 필요한 서빙 엔드포인트나 서명 URL 방식으로 교체해야 합니다.
