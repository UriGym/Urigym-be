package com.urigym.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Email and password are intentionally absent — they are not editable through
 * profile or admin updates (password has its own change endpoint).
 * <p>
 * All fields are optional and null-safe on the service side, since the profile form and
 * the notification-settings form both submit through this same DTO but only fill in
 * their own fields.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String fullName;
    private String phone;
    private String address;
    private Boolean notifyAnnouncements;
    private Boolean notifyMessages;
}
