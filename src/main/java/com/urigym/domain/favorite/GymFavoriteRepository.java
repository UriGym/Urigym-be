package com.urigym.domain.favorite;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymFavoriteRepository extends JpaRepository<GymFavorite, UUID> {

    boolean existsByUserIdAndGymId(UUID userId, UUID gymId);

    Optional<GymFavorite> findByUserIdAndGymId(UUID userId, UUID gymId);

    Page<GymFavorite> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByGymId(UUID gymId);
}
