package com.urigym.domain.gym;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public-facing lookups go through {@link GymSpecifications}, which filters out
 * admin-suspended gyms. The plain findById / findByOwnerId lookups stay unfiltered so
 * owners and admins can still reach a suspended gym.
 */
@Repository
public interface GymRepository extends JpaRepository<Gym, UUID>, JpaSpecificationExecutor<Gym> {

    List<Gym> findByOwnerId(UUID ownerId);

    Optional<Gym> findByKakaoPlaceId(String kakaoPlaceId);

    /**
     * Gyms within {@code radiusKm} of (lat, lng), nearest first. The bounding box args
     * narrow the scan to an indexable range before the exact Haversine distance (no
     * PostGIS in this project) filters and sorts it — a plain radius predicate can't use
     * an index at all.
     */
    @Query(value = """
            SELECT *, (
                6371 * acos(LEAST(1.0, GREATEST(-1.0,
                    cos(radians(:lat)) * cos(radians(g.lat)) * cos(radians(g.lng) - radians(:lng))
                    + sin(radians(:lat)) * sin(radians(g.lat))
                )))
            ) AS distance_km
            FROM gyms g
            WHERE g.lat BETWEEN :minLat AND :maxLat
              AND g.lng BETWEEN :minLng AND :maxLng
              AND (g.suspended_until IS NULL OR g.suspended_until <= :now)
              AND (
                6371 * acos(LEAST(1.0, GREATEST(-1.0,
                    cos(radians(:lat)) * cos(radians(g.lat)) * cos(radians(g.lng) - radians(:lng))
                    + sin(radians(:lat)) * sin(radians(g.lat))
                )))
              ) <= :radiusKm
            ORDER BY distance_km ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Gym> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng,
            @Param("now") LocalDateTime now,
            @Param("limit") int limit);
}
