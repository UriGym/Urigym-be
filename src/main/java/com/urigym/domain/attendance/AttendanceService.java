package com.urigym.domain.attendance;

import com.urigym.domain.gym.Gym;
import com.urigym.domain.gym.GymService;
import com.urigym.domain.member.GymMemberRepository;
import com.urigym.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final GymMemberRepository gymMemberRepository;
    private final GymService gymService;

    public Page<Attendance> getAttendancesByUser(UUID userId, Pageable pageable) {
        return attendanceRepository.findByUserIdOrderByCheckInTimeDesc(userId, pageable);
    }

    public Page<Attendance> getAttendancesByGym(UUID gymId, Pageable pageable) {
        return attendanceRepository.findByGymIdOrderByCheckInTimeDesc(gymId, pageable);
    }

    public List<Attendance> getAttendancesByUserAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        return attendanceRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    public long getTotalAttendanceCount(UUID userId) {
        return attendanceRepository.countByUserId(userId);
    }

    @Transactional
    public Attendance checkIn(UUID gymId, User user, String checkInMethod) {
        Gym gym = gymService.getGymById(gymId);

        // Verify membership (optional - can be relaxed for free gyms)
        if (!gymMemberRepository.existsByGymIdAndUserId(gymId, user.getId())) {
            throw new IllegalArgumentException("You are not a member of this gym");
        }

        Attendance attendance = Attendance.builder()
                .gym(gym)
                .user(user)
                .checkInTime(LocalDateTime.now())
                .checkInMethod(checkInMethod != null ? checkInMethod : "QR")
                .build();

        return attendanceRepository.save(attendance);
    }
}
