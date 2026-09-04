package com.urigym.domain.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findByGymIdOrderByEventDateDesc(UUID gymId, Pageable pageable);

    List<Event> findByGymIdAndEventDateGreaterThanEqualOrderByEventDateAsc(UUID gymId, LocalDate from);
}
