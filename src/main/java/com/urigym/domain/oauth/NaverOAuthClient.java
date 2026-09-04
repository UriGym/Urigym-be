package com.urigym.domain.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@Component
public class NaverOAuthClient implements SocialOAuthClient {

    private final RestClient restClient = RestClient.create("https://openapi.naver.com");

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.NAVER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OAuthUserInfo fetchUserInfo(String accessToken) {
        Map<String, Object> body;
        try {
            body = restClient.get()
                    .uri("/v1/nid/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            log.warn("Naver user info lookup failed: {}", e.getMessage());
            throw new IllegalArgumentException("네이버 인증에 실패했습니다.");
        }

        if (body == null || !"00".equals(body.get("resultcode"))) {
            throw new IllegalArgumentException("네이버 인증에 실패했습니다.");
        }

        Map<String, Object> response = (Map<String, Object>) body.get("response");
        if (response == null || response.get("id") == null) {
            throw new IllegalArgumentException("네이버 인증에 실패했습니다.");
        }

        return new OAuthUserInfo(
                String.valueOf(response.get("id")),
                (String) response.get("email"),
                (String) response.get("name")
        );
    }
}
