package com.urigym.domain.oauth;

/**
 * Verifies a provider access token (already issued to the frontend by the provider's own
 * SDK) and fetches the signed-in user's profile. Implementations call the provider's
 * "get my info" REST endpoint directly — no client secret is needed for this flow since
 * we're trusting a token the provider itself already validated at issuance.
 */
public interface SocialOAuthClient {

    OAuthProvider getProvider();

    /**
     * @throws IllegalArgumentException if the token is invalid, expired, or the provider
     *                                  call otherwise fails
     */
    OAuthUserInfo fetchUserInfo(String accessToken);
}
