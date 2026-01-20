package com.urigym.domain.user;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.user.entity.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
    }

    @PutMapping("/me")
    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyInfo(
            @AuthenticationPrincipal User user,
            @RequestBody User userDetails
    ) {
        User updatedUser = userService.updateUser(user.getId(), userDetails);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", UserResponse.from(updatedUser)));
    }
}
