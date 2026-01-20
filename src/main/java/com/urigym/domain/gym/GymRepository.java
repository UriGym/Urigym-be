package com.urigym.domain.gym;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GymRepository extends JpaRepository<Gym, UUID> {

    Page<Gym> findByCategory(String category, Pageable pageable);

    @Query("SELECT g FROM Gym g WHERE " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Gym> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<Gym> findByOwnerId(UUID ownerId);

    @Query("SELECT g FROM Gym g WHERE " +
           "g.lat BETWEEN :minLat AND :maxLat AND " +
           "g.lng BETWEEN :minLng AND :maxLng")
    List<Gym> findByLocationBounds(
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng
    );
}
