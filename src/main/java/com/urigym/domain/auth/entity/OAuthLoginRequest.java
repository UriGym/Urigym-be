package com.urigym.domain.auth.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** The access token the provider's own JS SDK already issued client-side. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthLoginRequest {

    @NotBlank(message = "액세스 토큰이 필요합니다.")
    private String accessToken;
}
