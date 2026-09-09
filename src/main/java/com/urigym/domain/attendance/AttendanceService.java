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

import java.time.LocalDate;
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
        return attendanceRepository.findByUserIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(userId, startDate, endDate);
    }

    public long getTotalAttendanceCount(UUID userId) {
        return attendanceRepository.countByUserId(userId);
    }

    public long countTodayAttendances(UUID gymId) {
        LocalDate today = LocalDate.now();
        return attendanceRepository.countByGymIdAndCheckInTimeBetween(
                gymId, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    @Transactional
    public Attendance checkIn(UUID gymId, User user, CheckInMethod method, String phoneNumber) {
        Gym gym = gymService.getGymById(gymId);

        if (!gymMemberRepository.existsByGymIdAndUserIdAndStatus(gymId, user.getId(), "ACTIVE")) {
            throw new IllegalArgumentException("해당 체육관의 관원이 아닙니다.");
        }

        verifyIdentity(user, method, phoneNumber);

        LocalDate today = LocalDate.now();
        if (attendanceRepository.existsByGymIdAndUserIdAndCheckInTimeBetween(
                gymId, user.getId(), today.atStartOfDay(), today.plusDays(1).atStartOfDay())) {
            throw new IllegalArgumentException("오늘은 이미 출석 체크를 완료했습니다.");
        }

        return attendanceRepository.save(Attendance.builder()
                .gym(gym)
                .user(user)
                .checkInTime(LocalDateTime.now())
                .checkInMethod(method.name())
                .build());
    }

    private void verifyIdentity(User user, CheckInMethod method, String phoneNumber) {
        switch (method) {
            case PHONE -> {
                if (phoneNumber == null || phoneNumber.isBlank()) {
                    throw new IllegalArgumentException("전화번호를 입력해주세요.");
                }
                if (user.getPhone() == null || !digitsOnly(user.getPhone()).equals(digitsOnly(phoneNumber))) {
                    throw new IllegalArgumentException("등록된 전화번호와 일치하지 않습니다.");
                }
            }
            case QR -> {
                // No extra verification: the QR code is scanned at the gym's own device.
            }
            case FACE, NFC -> throw new UnsupportedOperationException(
                    "해당 출석 방식은 준비 중입니다. 전화번호 출석을 이용해주세요.");

            // --- Face recognition (on hold) ---
            // Planned: the client posts a face descriptor, the server compares it against
            // the enrolled descriptor for this user and accepts above a similarity threshold.
            //
            // case FACE -> {
            //     if (faceDescriptor == null) throw new IllegalArgumentException("얼굴 정보가 필요합니다.");
            //     if (!faceRecognitionService.matches(user.getId(), faceDescriptor)) {
            //         throw new IllegalArgumentException("얼굴 인식에 실패했습니다.");
            //     }
            // }
            //
            // --- NFC (on hold) ---
            // Planned: the gym's NFC reader posts the tag id, which is matched against the
            // tag registered to this member.
            //
            // case NFC -> {
            //     if (nfcTagId == null) throw new IllegalArgumentException("NFC 태그 정보가 필요합니다.");
            //     if (!nfcTagService.belongsTo(user.getId(), nfcTagId)) {
            //         throw new IllegalArgumentException("등록되지 않은 NFC 태그입니다.");
            //     }
            // }
        }
    }

    private String digitsOnly(String value) {
        return value.replaceAll("\\D", "");
    }
}
