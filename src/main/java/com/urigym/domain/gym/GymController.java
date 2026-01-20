package com.urigym.domain.gym;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.gym.entity.GymResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gyms")
@RequiredArgsConstructor
@Tag(name = "Gym", description = "체육관 API")
public class GymController {

    private final GymService gymService;

    @GetMapping
    @Operation(summary = "체육관 목록 조회", description = "전체 체육관 목록을 페이징하여 조회합니다.")
    public ResponseEntity<ApiResponse<Page<GymResponse>>> getAllGyms(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<GymResponse> gyms = gymService.getAllGyms(pageable)
                .map(GymResponse::from);
        return ResponseEntity.ok(ApiResponse.success(gyms));
    }

    @GetMapping("/{id}")
    @Operation(summary = "체육관 상세 조회", description = "특정 체육관의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<GymResponse>> getGymById(@PathVariable UUID id) {
        Gym gym = gymService.getGymById(id);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.from(gym)));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "카테고리별 체육관 조회", description = "특정 카테고리의 체육관 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<GymResponse>>> getGymsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<GymResponse> gyms = gymService.getGymsByCategory(category, pageable)
                .map(GymResponse::from);
        return ResponseEntity.ok(ApiResponse.success(gyms));
    }

    @GetMapping("/search")
    @Operation(summary = "체육관 검색", description = "키워드로 체육관을 검색합니다.")
    public ResponseEntity<ApiResponse<Page<GymResponse>>> searchGyms(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<GymResponse> gyms = gymService.searchGyms(keyword, pageable)
                .map(GymResponse::from);
        return ResponseEntity.ok(ApiResponse.success(gyms));
    }

    @GetMapping("/location")
    @Operation(summary = "위치 기반 체육관 조회", description = "특정 위치 범위 내의 체육관을 조회합니다.")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getGymsByLocation(
            @RequestParam Double minLat,
            @RequestParam Double maxLat,
            @RequestParam Double minLng,
            @RequestParam Double maxLng
    ) {
        List<GymResponse> gyms = gymService.getGymsByLocation(minLat, maxLat, minLng, maxLng)
                .stream()
                .map(GymResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(gyms));
    }
}
