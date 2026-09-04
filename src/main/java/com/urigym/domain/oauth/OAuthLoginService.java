package com.urigym.domain.oauth;

import com.urigym.config.JwtTokenProvider;
import com.urigym.domain.auth.entity.AuthResponse;
import com.urigym.domain.user.User;
import com.urigym.domain.user.UserRepository;
import com.urigym.domain.user.entity.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Resolves a provider access token to one of our own users and issues our JWT — the
 * rest of the app (JwtAuthenticationFilter, AuthContext on the frontend) never needs to
 * know a login came from Kakao or Naver instead of a password.
 * <p>
 * Linking rule: if the social account's email matches an existing password-based user,
 * we link automatically rather than creating a duplicate account, on the assumption that
 * the provider has already verified that email belongs to this person.
 */
@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final UserRepository userRepository;
    private final UserOAuthAccountRepository oAuthAccountRepository;
    private final JwtTokenProvider tokenProvider;
    private final List<SocialOAuthClient> oAuthClients;

    @Transactional
    public AuthResponse login(OAuthProvider provider, String accessToken) {
        SocialOAuthClient client = oAuthClients.stream()
                .filter(c -> c.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 로그인 방식입니다: " + provider));

        OAuthUserInfo info = client.fetchUserInfo(accessToken);

        User user = oAuthAccountRepository.findByProviderAndProviderUserId(provider, info.providerUserId())
                .map(UserOAuthAccount::getUser)
                .orElseGet(() -> resolveOrCreateUser(provider, info));

        String token = tokenProvider.generateToken(user.getId(), user.getEmail());
        return AuthResponse.of(token, UserResponse.from(user));
    }

    private User resolveOrCreateUser(OAuthProvider provider, OAuthUserInfo info) {
        if (info.email() == null || info.email().isBlank()) {
            throw new IllegalArgumentException(
                    "이메일 제공에 동의해야 로그인할 수 있습니다. 카카오/네이버 설정에서 이메일 제공을 허용해주세요.");
        }

        User user = userRepository.findByEmail(info.email())
                .orElseGet(newSocialUser(info));

        linkAccount(user, provider, info.providerUserId());
        return user;
    }

    private Supplier<User> newSocialUser(OAuthUserInfo info) {
        return () -> userRepository.save(User.builder()
                .email(info.email())
                .password(null)
                .fullName(info.nickname())
                .build());
    }

    private void linkAccount(User user, OAuthProvider provider, String providerUserId) {
        oAuthAccountRepository.save(UserOAuthAccount.builder()
                .user(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .build());
    }
}
