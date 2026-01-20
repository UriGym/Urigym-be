package com.urigym.domain.attendance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    Page<Attendance> findByUserIdOrderByCheckInTimeDesc(UUID userId, Pageable pageable);

    Page<Attendance> findByGymIdOrderByCheckInTimeDesc(UUID gymId, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.user.id = :userId AND " +
           "a.checkInTime BETWEEN :startDate AND :endDate ORDER BY a.checkInTime DESC")
    List<Attendance> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    long countByUserId(UUID userId);

    long countByGymId(UUID gymId);
}
