package com.urigym.domain.user.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneVerifyRequest {

    @NotBlank(message = "전화번호를 입력해주세요.")
    private String phone;
}
