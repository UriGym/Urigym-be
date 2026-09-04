package com.urigym.domain.announcement;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.announcement.entity.AnnouncementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Announcement", description = "공지사항 API")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/gyms/{gymId}/announcements")
    @Operation(summary = "체육관 공지사항 조회", description = "특정 체육관의 공지사항 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<AnnouncementResponse>>> getAnnouncementsByGym(
            @PathVariable UUID gymId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<AnnouncementResponse> announcements = announcementService.getAnnouncementsByGym(gymId, pageable)
                .map(AnnouncementResponse::from);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @GetMapping("/announcements/{id}")
    @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항의 상세 내용을 조회합니다.")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> getAnnouncementById(@PathVariable UUID id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        return ResponseEntity.ok(ApiResponse.success(AnnouncementResponse.from(announcement)));
    }
}
