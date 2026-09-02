package com.urigym.domain.member.entity;

import com.urigym.domain.member.GymMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymMemberResponse {

    private UUID id;
    private UUID gymId;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastCheckInTime;
    private Long attendanceCount;
    /** Check-ins as a share of days since joining, 0..100. */
    private Integer attendanceRate;
    private Long daysSinceLastCheckIn;

    public static GymMemberResponse from(GymMember member) {
        return builderOf(member).build();
    }

    public static GymMemberResponse withStats(GymMember member, LocalDateTime lastCheckIn, long attendanceCount) {
        long daysEnrolled = Math.max(1, java.time.Duration.between(member.getJoinedAt(), LocalDateTime.now()).toDays());
        int rate = (int) Math.min(100, Math.round(attendanceCount * 100.0 / daysEnrolled));

        return builderOf(member)
                .lastCheckInTime(lastCheckIn)
                .attendanceCount(attendanceCount)
                .attendanceRate(rate)
                .daysSinceLastCheckIn(lastCheckIn == null
                        ? null
                        : java.time.Duration.between(lastCheckIn, LocalDateTime.now()).toDays())
                .build();
    }

    private static GymMemberResponseBuilder builderOf(GymMember member) {
        return GymMemberResponse.builder()
                .id(member.getId())
                .gymId(member.getGym().getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getFullName())
                .userEmail(member.getUser().getEmail())
                .userPhone(member.getUser().getPhone())
                .status(member.getStatus())
                .joinedAt(member.getJoinedAt())
                .expiresAt(member.getExpiresAt());
    }
}
