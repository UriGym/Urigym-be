package com.urigym.domain.oauth;

/**
 * Normalized profile pulled from a social provider's "get my info" API.
 *
 * @param email may be null — Kakao/Naver only return it when the user consented to the
 *              email scope, and Kakao additionally requires the app to have that scope
 *              approved in its console.
 */
public record OAuthUserInfo(String providerUserId, String email, String nickname) {
}
