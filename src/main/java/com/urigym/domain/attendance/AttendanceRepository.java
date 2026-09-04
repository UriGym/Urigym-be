package com.urigym.domain.attendance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    Page<Attendance> findByUserIdOrderByCheckInTimeDesc(UUID userId, Pageable pageable);

    Page<Attendance> findByGymIdOrderByCheckInTimeDesc(UUID gymId, Pageable pageable);

    List<Attendance> findByUserIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(
            UUID userId, LocalDateTime start, LocalDateTime end
    );

    long countByUserId(UUID userId);

    long countByGymId(UUID gymId);

    long countByGymIdAndCheckInTimeBetween(UUID gymId, LocalDateTime start, LocalDateTime end);

    boolean existsByGymIdAndUserIdAndCheckInTimeBetween(
            UUID gymId, UUID userId, LocalDateTime start, LocalDateTime end
    );

    long countByGymIdAndUserId(UUID gymId, UUID userId);

    Optional<Attendance> findTopByGymIdAndUserIdOrderByCheckInTimeDesc(UUID gymId, UUID userId);
}
