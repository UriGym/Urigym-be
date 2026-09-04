package com.urigym.domain.auth;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.auth.entity.AuthResponse;
import com.urigym.domain.auth.entity.LoginRequest;
import com.urigym.domain.auth.entity.OAuthLoginRequest;
import com.urigym.domain.auth.entity.SignupRequest;
import com.urigym.domain.oauth.OAuthLoginService;
import com.urigym.domain.oauth.OAuthProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;
    private final OAuthLoginService oAuthLoginService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.ok(ApiResponse.success("Signup successful", response));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/oauth/{provider}")
    @Operation(summary = "소셜 로그인", description = "카카오/네이버 등에서 발급받은 액세스 토큰으로 로그인하거나 계정을 새로 만듭니다.")
    public ResponseEntity<ApiResponse<AuthResponse>> oauthLogin(
            @PathVariable OAuthProvider provider,
            @Valid @RequestBody OAuthLoginRequest request
    ) {
        AuthResponse response = oAuthLoginService.login(provider, request.getAccessToken());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
