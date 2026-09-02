package com.urigym.domain.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByGymIdOrderByCreatedAtDesc(UUID gymId, Pageable pageable);

    Page<Review> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Review> findByGymIdIn(List<UUID> gymIds);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.gym.id = :gymId")
    Double getAverageRatingByGymId(@Param("gymId") UUID gymId);

    long countByGymId(UUID gymId);

    boolean existsByGymIdAndUserId(UUID gymId, UUID userId);
}
