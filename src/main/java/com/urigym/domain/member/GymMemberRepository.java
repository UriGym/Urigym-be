package com.urigym.domain.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymMemberRepository extends JpaRepository<GymMember, UUID> {

    Page<GymMember> findByGymId(UUID gymId, Pageable pageable);

    List<GymMember> findByGymId(UUID gymId);

    Page<GymMember> findByUserId(UUID userId, Pageable pageable);

    List<GymMember> findByUserId(UUID userId);

    Optional<GymMember> findByGymIdAndUserId(UUID gymId, UUID userId);

    boolean existsByGymIdAndUserId(UUID gymId, UUID userId);

    long countByGymIdAndStatus(UUID gymId, String status);

    long countByGymId(UUID gymId);

    List<GymMember> findByIdIn(List<UUID> ids);

    /**
     * Last check-in per member of a gym. Members who never checked in are included
     * with a null timestamp, so the caller can treat them as long-absent.
     */
    @Query("""
            SELECT m.id, MAX(a.checkInTime)
            FROM GymMember m
            LEFT JOIN Attendance a ON a.user.id = m.user.id AND a.gym.id = m.gym.id
            WHERE m.gym.id = :gymId
            GROUP BY m.id
            """)
    List<Object[]> findLastCheckInByGymId(@Param("gymId") UUID gymId);

    /** Total check-ins per member of a gym, for the attendance-rate column. */
    @Query("""
            SELECT m.id, COUNT(a.id)
            FROM GymMember m
            LEFT JOIN Attendance a ON a.user.id = m.user.id AND a.gym.id = m.gym.id
            WHERE m.gym.id = :gymId
            GROUP BY m.id
            """)
    List<Object[]> findAttendanceCountByGymId(@Param("gymId") UUID gymId);
}
