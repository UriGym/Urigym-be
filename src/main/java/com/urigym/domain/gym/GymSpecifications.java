package com.urigym.domain.gym;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Composable predicates for gym lookups.
 * <p>
 * These exist instead of derived query methods because the visibility rule is an OR
 * over two conditions. Spring Data method names have no grouping, so a name like
 * {@code findByCategoryAndSuspendedUntilIsNullOrSuspendedUntilBefore} would parse as
 * {@code (category AND not suspended) OR (suspension expired)} and leak suspended gyms
 * from other categories. Combining specifications keeps the OR correctly grouped.
 */
public final class GymSpecifications {

    private GymSpecifications() {
    }

    /** Gym is not currently hidden by an admin suspension. */
    public static Specification<Gym> visible(LocalDateTime now) {
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("suspendedUntil")),
                cb.lessThanOrEqualTo(root.get("suspendedUntil"), now)
        );
    }

    public static Specification<Gym> hasCategory(String category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    /** Matches the keyword against the gym name or address, case-insensitively. */
    public static Specification<Gym> matchesKeyword(String keyword) {
        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("address")), pattern)
        );
    }

    public static Specification<Gym> withinBounds(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        return (root, query, cb) -> cb.and(
                cb.between(root.get("lat"), minLat, maxLat),
                cb.between(root.get("lng"), minLng, maxLng)
        );
    }

    /**
     * Gym has an owner, i.e. it was registered by an owner (source=USER) rather than
     * sitting unclaimed after a Kakao import.
     * <p>
     * Currently unused: public read APIs used to filter on this to hide the ~36k
     * unclaimed Kakao rows, but that policy was reverted (2026-09-06) since it made
     * every region outside Siheung Wolgot-dong show zero gyms. Kept around in case
     * the "claimed only" policy comes back.
     */
    public static Specification<Gym> claimed() {
        return (root, query, cb) -> cb.isNotNull(root.get("owner"));
    }
}
