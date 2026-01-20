package com.urigym.domain.attendance;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.attendance.entity.AttendanceResponse;
import com.urigym.domain.attendance.entity.CheckInRequest;
import com.urigym.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "출석 API")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @Operation(summary = "내 출석 기록 조회", description = "로그인한 사용자의 출석 기록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getMyAttendances(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AttendanceResponse> attendances = attendanceService.getAttendancesByUser(user.getId(), pageable)
                .map(AttendanceResponse::from);
        return ResponseEntity.ok(ApiResponse.success(attendances));
    }

    @GetMapping("/monthly")
    @Operation(summary = "월간 출석 기록 조회", description = "특정 월의 출석 기록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getMonthlyAttendances(
            @AuthenticationPrincipal User user,
            @RequestParam int year,
            @RequestParam int month
    ) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);

        List<AttendanceResponse> attendances = attendanceService
                .getAttendancesByUserAndDateRange(user.getId(), startDate, endDate)
                .stream()
                .map(AttendanceResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(attendances));
    }

    @GetMapping("/count")
    @Operation(summary = "총 출석 횟수 조회", description = "사용자의 총 출석 횟수를 조회합니다.")
    public ResponseEntity<ApiResponse<Long>> getTotalAttendanceCount(@AuthenticationPrincipal User user) {
        long count = attendanceService.getTotalAttendanceCount(user.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping
    @Operation(summary = "출석 체크", description = "체육관에 출석 체크합니다.")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CheckInRequest request
    ) {
        Attendance attendance = attendanceService.checkIn(
                request.getGymId(),
                user,
                request.getCheckInMethod()
        );
        return ResponseEntity.ok(ApiResponse.success("Check-in successful", AttendanceResponse.from(attendance)));
    }
}
