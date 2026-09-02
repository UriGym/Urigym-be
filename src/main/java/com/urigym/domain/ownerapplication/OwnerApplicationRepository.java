package com.urigym.domain.ownerapplication;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OwnerApplicationRepository extends JpaRepository<OwnerApplication, UUID> {

    Optional<OwnerApplication> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByUserIdAndStatus(UUID userId, ApplicationStatus status);

    Page<OwnerApplication> findByStatusOrderByCreatedAtAsc(ApplicationStatus status, Pageable pageable);

    Page<OwnerApplication> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
