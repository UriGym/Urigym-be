package com.urigym.domain.favorite;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.favorite.entity.FavoriteStatusResponse;
import com.urigym.domain.gym.entity.GymResponse;
import com.urigym.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Favorite", description = "체육관 찜하기 API")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/gyms/{gymId}/favorite")
    @Operation(summary = "찜 상태 조회", description = "현재 로그인한 사용자의 찜 여부와 총 찜 수를 조회합니다.")
    public ResponseEntity<ApiResponse<FavoriteStatusResponse>> getFavoriteStatus(
            @PathVariable UUID gymId,
            @AuthenticationPrincipal User user
    ) {
        boolean favorited = favoriteService.isFavorited(gymId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(
                FavoriteStatusResponse.builder().favorited(favorited).build()));
    }

    @PostMapping("/gyms/{gymId}/favorite")
    @Operation(summary = "찜 토글", description = "체육관 찜 상태를 켜거나 끕니다.")
    public ResponseEntity<ApiResponse<FavoriteStatusResponse>> toggleFavorite(
            @PathVariable UUID gymId,
            @AuthenticationPrincipal User user
    ) {
        boolean favorited = favoriteService.toggleFavorite(gymId, user);
        return ResponseEntity.ok(ApiResponse.success(
                FavoriteStatusResponse.builder().favorited(favorited).build()));
    }

    @GetMapping("/me/favorites")
    @Operation(summary = "내 찜 목록 조회", description = "찜한 체육관 목록을 페이징하여 조회합니다.")
    public ResponseEntity<ApiResponse<Page<GymResponse>>> getMyFavorites(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<GymResponse> favorites = favoriteService.getMyFavorites(user.getId(), pageable)
                .map(favorite -> GymResponse.from(favorite.getGym()));
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }
}
