package com.urigym.domain.member.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GymMemberRequest {

    /** Existing account to enrol — members must already have signed up. */
    @NotBlank(message = "회원 이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String userEmail;

    private String status;

    private LocalDateTime expiresAt;
}
