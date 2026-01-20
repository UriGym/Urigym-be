package com.urigym.domain.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findByGymIdOrderByEventDateDesc(UUID gymId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.gym.id = :gymId AND e.eventDate >= :today ORDER BY e.eventDate ASC")
    List<Event> findUpcomingEventsByGymId(@Param("gymId") UUID gymId, @Param("today") LocalDate today);
}
