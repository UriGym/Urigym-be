package com.urigym.domain.review;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.review.entity.ReviewCreateRequest;
import com.urigym.domain.review.entity.ReviewResponse;
import com.urigym.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Review", description = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/gyms/{gymId}/reviews")
    @Operation(summary = "체육관 리뷰 조회", description = "특정 체육관의 리뷰 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByGym(
            @PathVariable UUID gymId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ReviewResponse> reviews = reviewService.getReviewsByGym(gymId, pageable)
                .map(ReviewResponse::from);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @PostMapping("/gyms/{gymId}/reviews")
    @Operation(summary = "리뷰 작성", description = "체육관에 리뷰를 작성합니다.")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable UUID gymId,
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        Review review = reviewService.createReview(gymId, user, request.getRating(), request.getContent());
        return ResponseEntity.ok(ApiResponse.success("Review created successfully", ReviewResponse.from(review)));
    }

    @PutMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 수정", description = "자신이 작성한 리뷰를 수정합니다.")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        Review review = reviewService.updateReview(reviewId, user.getId(), request.getRating(), request.getContent());
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", ReviewResponse.from(review)));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 삭제", description = "자신이 작성한 리뷰를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal User user
    ) {
        reviewService.deleteReview(reviewId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}
