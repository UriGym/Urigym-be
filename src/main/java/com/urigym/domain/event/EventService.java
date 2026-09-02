package com.urigym.domain.event;

import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.domain.event.entity.EventRequest;
import com.urigym.domain.gym.Gym;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public List<Event> getUpcomingEvents(UUID gymId) {
        return eventRepository.findByGymIdAndEventDateGreaterThanEqualOrderByEventDateAsc(gymId, LocalDate.now());
    }

    public Event getEventById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    @Transactional
    public Event create(Gym gym, EventRequest request) {
        return eventRepository.save(Event.builder()
                .gym(gym)
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .build());
    }

    @Transactional
    public void delete(UUID id) {
        eventRepository.delete(getEventById(id));
    }
}
