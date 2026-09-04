package com.urigym.domain.membershipplan.entity;

import com.urigym.domain.membershipplan.MembershipPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlanResponse {

    private UUID id;
    private UUID gymId;
    private String name;
    private Integer price;
    private String description;

    public static MembershipPlanResponse from(MembershipPlan plan) {
        return MembershipPlanResponse.builder()
                .id(plan.getId())
                .gymId(plan.getGym().getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .description(plan.getDescription())
                .build();
    }
}
