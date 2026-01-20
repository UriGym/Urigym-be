package com.urigym.domain.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymMemberRepository extends JpaRepository<GymMember, UUID> {

    Page<GymMember> findByGymId(UUID gymId, Pageable pageable);

    Page<GymMember> findByUserId(UUID userId, Pageable pageable);

    Optional<GymMember> findByGymIdAndUserId(UUID gymId, UUID userId);

    boolean existsByGymIdAndUserId(UUID gymId, UUID userId);

    long countByGymIdAndStatus(UUID gymId, String status);
}
