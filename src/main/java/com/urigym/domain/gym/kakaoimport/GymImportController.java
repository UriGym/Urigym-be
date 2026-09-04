package com.urigym.domain.gym.kakaoimport;

import com.urigym.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/gyms/import/kakao")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Gym Import", description = "카카오 로컬 API 기반 전국 체육관 임포트")
public class GymImportController {

    private final GymImportService gymImportService;

    @PostMapping
    @Operation(summary = "임포트 시작", description = "전국 격자 x 카테고리 키워드로 카카오 장소 검색을 돌려 미등록 체육관을 채운다. 오래 걸리므로 비동기로 시작만 하고 즉시 응답한다.")
    public ResponseEntity<ApiResponse<String>> start() {
        boolean started = gymImportService.start();
        return ResponseEntity.ok(ApiResponse.success(started ? "임포트를 시작했습니다." : "이미 진행 중입니다."));
    }

    @GetMapping("/status")
    @Operation(summary = "임포트 진행 상황", description = "완료 유닛 수, 생성/스킵/에러 카운트를 반환한다.")
    public ResponseEntity<ApiResponse<GymImportService.Status>> status() {
        return ResponseEntity.ok(ApiResponse.success(gymImportService.status()));
    }
}
