package com.urigym.domain.member.entity;

import com.urigym.domain.gym.entity.GymResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Separate from GymMemberResponse on purpose: that DTO is shaped for an owner looking at
 * one member, this one is shaped for a user looking across their own gyms — the fields
 * that matter (gym info) and don't (their own name/email) are the opposite of each other.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyMembershipResponse {

    /** GymMember id — pass this to the cancel endpoint. */
    private UUID id;
    private GymResponse gym;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastCheckInTime;
    private Long attendanceCount;
}
