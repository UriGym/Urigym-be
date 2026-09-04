package com.urigym.domain.ownerapplication;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.ownerapplication.entity.OwnerApplicationRequest;
import com.urigym.domain.ownerapplication.entity.OwnerApplicationResponse;
import com.urigym.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner-applications")
@RequiredArgsConstructor
@Tag(name = "OwnerApplication", description = "관장 등록 신청 API")
public class OwnerApplicationController {

    private final OwnerApplicationService ownerApplicationService;

    @PostMapping
    @Operation(summary = "관장 등록 신청", description = "사업자등록증과 관장 자격증을 제출해 관장 권한을 신청합니다.")
    public ResponseEntity<ApiResponse<OwnerApplicationResponse>> apply(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OwnerApplicationRequest request
    ) {
        OwnerApplication application = ownerApplicationService.apply(user, request);
        return ResponseEntity.ok(ApiResponse.success(
                "신청이 접수되었습니다. 관리자 확인 후 승인됩니다.",
                OwnerApplicationResponse.from(application)
        ));
    }

    @GetMapping("/me")
    @Operation(summary = "내 신청 상태 조회", description = "가장 최근에 제출한 관장 등록 신청의 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<OwnerApplicationResponse>> getMyApplication(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
                ownerApplicationService.getLatestApplication(user.getId())
                        .map(OwnerApplicationResponse::from)
                        .orElse(null)
        ));
    }
}
