package com.urigym.domain.membershipplan.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlanRequest {

    @NotBlank(message = "회원권 이름을 입력해주세요.")
    private String name;

    @NotNull(message = "가격을 입력해주세요.")
    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    private String description;
}
