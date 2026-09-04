package com.urigym.domain.user;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.user.entity.ChangePasswordRequest;
import com.urigym.domain.user.entity.PhoneConfirmRequest;
import com.urigym.domain.user.entity.PhoneVerifyRequest;
import com.urigym.domain.user.entity.UserResponse;
import com.urigym.domain.user.entity.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 API")
public class UserController {

    private final UserService userService;
    private final PhoneVerificationService phoneVerificationService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
    }

    @PutMapping("/me")
    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyInfo(
            @AuthenticationPrincipal User user,
            @RequestBody UserUpdateRequest request
    ) {
        User updatedUser = userService.updateUser(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", UserResponse.from(updatedUser)));
    }

    @PutMapping("/me/password")
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인한 뒤 새 비밀번호로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다.", null));
    }

    @PostMapping("/me/phone/verify-code")
    @Operation(summary = "전화번호 인증번호 발송",
            description = "SMS 연동 전까지는 실제 문자 대신 서버 로그에 인증번호를 남깁니다.")
    public ResponseEntity<ApiResponse<Void>> sendPhoneVerificationCode(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PhoneVerifyRequest request
    ) {
        phoneVerificationService.sendCode(user.getId(), request.getPhone());
        return ResponseEntity.ok(ApiResponse.success("인증번호를 발송했습니다.", null));
    }

    @PostMapping("/me/phone/confirm-code")
    @Operation(summary = "전화번호 인증번호 확인", description = "인증에 성공하면 전화번호가 저장되고 인증 완료로 표시됩니다.")
    public ResponseEntity<ApiResponse<UserResponse>> confirmPhoneVerificationCode(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PhoneConfirmRequest request
    ) {
        phoneVerificationService.confirmCode(user.getId(), request.getPhone(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success(
                "전화번호 인증이 완료되었습니다.",
                UserResponse.from(userService.getUserById(user.getId()))
        ));
    }
}
