package com.urigym.domain.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Admin search across the three identifying fields; all-OR, so no grouping issue. */
    Page<User> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrPhoneContaining(
            String email, String fullName, String phone, Pageable pageable
    );
}
