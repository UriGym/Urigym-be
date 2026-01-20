package com.urigym.domain.attendance.entity;

import com.urigym.domain.attendance.Attendance;
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
public class AttendanceResponse {

    private UUID id;
    private UUID gymId;
    private String gymName;
    private LocalDateTime checkInTime;
    private String checkInMethod;

    public static AttendanceResponse from(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .gymId(attendance.getGym().getId())
                .gymName(attendance.getGym().getName())
                .checkInTime(attendance.getCheckInTime())
                .checkInMethod(attendance.getCheckInMethod())
                .build();
    }
}
