package com.urigym.domain.user.entity;

import com.urigym.domain.oauth.OAuthProvider;
import com.urigym.domain.oauth.UserOAuthAccount;
import com.urigym.domain.user.AppRole;
import com.urigym.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private Boolean phoneVerified;
    private String address;
    private String avatarUrl;
    private AppRole role;
    private Boolean notifyAnnouncements;
    private Boolean notifyMessages;
    /** False for accounts created purely through social login — see UserOAuthAccount. */
    private Boolean hasPassword;
    /** Linked social login providers, e.g. [KAKAO]. Empty for password-only accounts. */
    private List<OAuthProvider> oauthProviders;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .phoneVerified(user.getPhoneVerified())
                .address(user.getAddress())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .notifyAnnouncements(user.getNotifyAnnouncements())
                .notifyMessages(user.getNotifyMessages())
                .hasPassword(user.getPassword() != null)
                .oauthProviders(user.getOauthAccounts().stream().map(UserOAuthAccount::getProvider).distinct().toList())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
