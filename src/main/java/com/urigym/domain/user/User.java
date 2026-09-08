package com.urigym.domain.user;

import com.urigym.domain.oauth.UserOAuthAccount;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    /** Null for accounts created purely through social login — see {@link com.urigym.domain.oauth.UserOAuthAccount}. */
    private String password;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    private String address;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "notify_announcements", nullable = false)
    @Builder.Default
    private Boolean notifyAnnouncements = true;

    @Column(name = "notify_messages", nullable = false)
    @Builder.Default
    private Boolean notifyMessages = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppRole role = AppRole.USER;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // EAGER, not LAZY: JwtAuthenticationFilter loads the principal in its own short-lived
    // session before the request's OSIV session opens, so a lazy proxy here throws
    // "no Session" the moment a controller touches it. Table is at most a couple of rows
    // per user, so the extra per-load select is cheap.
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    @Builder.Default
    private List<UserOAuthAccount> oauthAccounts = new ArrayList<>();
}
