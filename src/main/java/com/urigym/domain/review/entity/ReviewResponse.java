package com.urigym.domain.review.entity;

import com.urigym.domain.review.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private UUID id;
    private UUID gymId;
    private UUID userId;
    private String userName;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .gymId(review.getGym().getId())
                .userId(review.getUser().getId())
                .userName(maskName(review.getUser().getFullName()))
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private static String maskName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return "익명";
        }
        if (fullName.length() <= 1) {
            return fullName + "*";
        }
        return fullName.charAt(0) + "*" + fullName.substring(fullName.length() - 1);
    }
}
