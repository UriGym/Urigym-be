package com.urigym.domain.event.entity;

import com.urigym.domain.event.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private UUID id;
    private UUID gymId;
    private String title;
    private String description;
    private LocalDate eventDate;
    private LocalDateTime createdAt;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .gymId(event.getGym().getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
