package com.urigym.domain.event;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.event.entity.EventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gyms/{gymId}/events")
@RequiredArgsConstructor
@Tag(name = "Event", description = "체육관 이벤트 API")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "예정된 이벤트 조회", description = "특정 체육관의 다가오는 이벤트를 조회합니다.")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getUpcomingEvents(@PathVariable UUID gymId) {
        List<EventResponse> events = eventService.getUpcomingEvents(gymId)
                .stream()
                .map(EventResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(events));
    }
}
