package com.urigym.domain.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@Component
public class KakaoOAuthClient implements SocialOAuthClient {

    private final RestClient restClient = RestClient.create("https://kapi.kakao.com");

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OAuthUserInfo fetchUserInfo(String accessToken) {
        Map<String, Object> body;
        try {
            body = restClient.get()
                    .uri("/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            log.warn("Kakao user info lookup failed: {}", e.getMessage());
            throw new IllegalArgumentException("카카오 인증에 실패했습니다.");
        }

        if (body == null || body.get("id") == null) {
            throw new IllegalArgumentException("카카오 인증에 실패했습니다.");
        }

        String providerUserId = String.valueOf(body.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) body.getOrDefault("kakao_account", Map.of());
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.getOrDefault("profile", Map.of());

        String email = (String) kakaoAccount.get("email");
        String nickname = (String) profile.get("nickname");

        return new OAuthUserInfo(providerUserId, email, nickname);
    }
}
