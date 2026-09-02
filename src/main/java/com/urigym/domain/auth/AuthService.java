package com.urigym.domain.auth;

import com.urigym.config.JwtTokenProvider;
import com.urigym.domain.auth.entity.AuthResponse;
import com.urigym.domain.auth.entity.LoginRequest;
import com.urigym.domain.auth.entity.SignupRequest;
import com.urigym.domain.user.User;
import com.urigym.domain.user.UserService;
import com.urigym.domain.user.entity.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        User savedUser = userService.createUser(user);

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail());
        return AuthResponse.of(token, UserResponse.from(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userService.getUserByEmail(request.getEmail());

        if (user.getPassword() == null) {
            throw new IllegalArgumentException("소셜 로그인으로 가입된 계정입니다. 카카오 또는 네이버로 로그인해주세요.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail());
        return AuthResponse.of(token, UserResponse.from(user));
    }
}
