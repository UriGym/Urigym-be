package com.urigym.domain.admin;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.admin.entity.AdminReviewRequest;
import com.urigym.domain.admin.entity.ReportStatusRequest;
import com.urigym.domain.gym.GymService;
import com.urigym.domain.gym.entity.GymResponse;
import com.urigym.domain.ownerapplication.ApplicationStatus;
import com.urigym.domain.ownerapplication.OwnerApplicationService;
import com.urigym.domain.ownerapplication.entity.OwnerApplicationResponse;
import com.urigym.domain.report.ReportService;
import com.urigym.domain.report.ReportStatus;
import com.urigym.domain.report.entity.ReportResponse;
import com.urigym.domain.user.UserService;
import com.urigym.domain.user.entity.UserResponse;
import com.urigym.domain.user.entity.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin-only surface. Owner applications are approved here and nowhere else —
 * there is deliberately no automatic promotion path.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "관리자 전용 API")
public class AdminController {

    private final UserService userService;
    private final OwnerApplicationService ownerApplicationService;
    private final ReportService reportService;
    private final GymService gymService;

    // --- Users ---

    @GetMapping("/users")
    @Operation(summary = "사용자 목록", description = "이메일/이름/전화번호로 검색하며 사용자 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUsers(keyword, pageable).map(UserResponse::from)
        ));
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "사용자 수정", description = "이름, 전화번호, 주소만 수정할 수 있습니다. 이메일과 비밀번호는 변경 불가합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID userId,
            @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "수정되었습니다.",
                UserResponse.from(userService.updateUser(userId, request))
        ));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "사용자 삭제", description = "사용자 계정을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("삭제되었습니다.", null));
    }

    // --- Owner applications ---

    @GetMapping("/owner-applications")
    @Operation(summary = "관장 신청 목록", description = "관장 등록 신청 목록을 조회합니다. status 미지정 시 전체를 반환합니다.")
    public ResponseEntity<ApiResponse<Page<OwnerApplicationResponse>>> getOwnerApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                ownerApplicationService.getApplications(status, pageable).map(OwnerApplicationResponse::from)
        ));
    }

    @PutMapping("/owner-applications/{applicationId}")
    @Operation(summary = "관장 신청 승인 / 반려",
            description = "제출된 사업자등록증과 자격증을 확인한 뒤 관리자가 직접 승인하거나 반려합니다.")
    public ResponseEntity<ApiResponse<OwnerApplicationResponse>> reviewOwnerApplication(
            @PathVariable UUID applicationId,
            @RequestBody AdminReviewRequest request
    ) {
        OwnerApplicationResponse response = OwnerApplicationResponse.from(
                ownerApplicationService.review(applicationId, request.isApprove(), request.getAdminNote())
        );
        return ResponseEntity.ok(ApiResponse.success(
                request.isApprove() ? "관장 권한이 부여되었습니다." : "신청이 반려되었습니다.",
                response
        ));
    }

    // --- Reports and inquiries ---

    @GetMapping("/reports")
    @Operation(summary = "신고 / 문의 목록", description = "접수된 신고와 문의를 조회합니다.")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getReports(status, pageable).map(ReportResponse::from)
        ));
    }

    @PutMapping("/reports/{reportId}")
    @Operation(summary = "신고 / 문의 처리", description = "신고 또는 문의의 처리 상태를 변경합니다.")
    public ResponseEntity<ApiResponse<ReportResponse>> updateReportStatus(
            @PathVariable UUID reportId,
            @Valid @RequestBody ReportStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "처리되었습니다.",
                ReportResponse.from(reportService.updateStatus(reportId, request.getStatus(), request.getAdminNote()))
        ));
    }

    // --- Gym suspension ---

    @PutMapping("/gyms/{gymId}/suspend")
    @Operation(summary = "체육관 노출 정지",
            description = "가격 불일치나 신고 누적 시 지정한 일수 동안 목록과 지도에서 노출을 중단합니다.")
    public ResponseEntity<ApiResponse<GymResponse>> suspendGym(
            @PathVariable UUID gymId,
            @RequestParam int days
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                days + "일간 노출이 정지되었습니다.",
                GymResponse.from(gymService.suspend(gymId, days))
        ));
    }

    @PutMapping("/gyms/{gymId}/unsuspend")
    @Operation(summary = "체육관 노출 정지 해제", description = "노출 정지를 즉시 해제합니다.")
    public ResponseEntity<ApiResponse<GymResponse>> unsuspendGym(@PathVariable UUID gymId) {
        return ResponseEntity.ok(ApiResponse.success(
                "정지가 해제되었습니다.",
                GymResponse.from(gymService.unsuspend(gymId))
        ));
    }

    @GetMapping("/gyms")
    @Operation(summary = "전체 체육관 목록", description = "정지된 체육관을 포함한 전체 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<GymResponse>>> getAllGyms(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                gymService.getAllGymsIncludingSuspended(pageable).map(GymResponse::from)
        ));
    }
}
