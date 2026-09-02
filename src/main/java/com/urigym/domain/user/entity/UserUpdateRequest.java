package com.urigym.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Email and password are intentionally absent — they are not editable through
 * profile or admin updates.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String fullName;
    private String phone;
    private String address;
}
