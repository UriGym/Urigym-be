package com.urigym.domain.attendance.entity;

import com.urigym.domain.attendance.CheckInMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {

    @NotNull(message = "체육관을 선택해주세요.")
    private UUID gymId;

    @NotNull(message = "출석 방식을 선택해주세요.")
    private CheckInMethod checkInMethod;

    /** Required when checkInMethod is PHONE. */
    private String phoneNumber;
}
