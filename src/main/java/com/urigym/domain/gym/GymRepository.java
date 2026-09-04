package com.urigym.domain.gym;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Public-facing lookups go through {@link GymSpecifications}, which filters out
 * admin-suspended gyms. The plain findById / findByOwnerId lookups stay unfiltered so
 * owners and admins can still reach a suspended gym.
 */
@Repository
public interface GymRepository extends JpaRepository<Gym, UUID>, JpaSpecificationExecutor<Gym> {

    List<Gym> findByOwnerId(UUID ownerId);
}
