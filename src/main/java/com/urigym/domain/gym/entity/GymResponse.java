package com.urigym.domain.gym.entity;

import com.urigym.domain.gym.Gym;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymResponse {

    private UUID id;
    private String name;
    private String category;
    private String address;
    private String description;
    private String phone;
    private String imageUrl;
    private Boolean isOpen;
    private Double lat;
    private Double lng;
    private Integer memberCount;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer priceMin;
    private Integer priceMax;
    private List<String> tags;
    private Integer reportCount;
    private LocalDateTime suspendedUntil;
    private UUID ownerId;
    /** Only populated by the ranked listing. */
    private Double rankScore;

    public static GymResponse from(Gym gym) {
        return builderOf(gym).build();
    }

    public static GymResponse ranked(Gym gym, double rankScore) {
        return builderOf(gym).rankScore(rankScore).build();
    }

    private static GymResponseBuilder builderOf(Gym gym) {
        return GymResponse.builder()
                .id(gym.getId())
                .name(gym.getName())
                .category(gym.getCategory())
                .address(gym.getAddress())
                .description(gym.getDescription())
                .phone(gym.getPhone())
                .imageUrl(gym.getImageUrl())
                .isOpen(gym.getIsOpen())
                .lat(gym.getLat())
                .lng(gym.getLng())
                .memberCount(gym.getMemberCount())
                .rating(gym.getRating())
                .reviewCount(gym.getReviewCount())
                .priceMin(gym.getPriceMin())
                .priceMax(gym.getPriceMax())
                .tags(gym.getTags())
                .reportCount(gym.getReportCount())
                .suspendedUntil(gym.getSuspendedUntil())
                .ownerId(gym.getOwner() != null ? gym.getOwner().getId() : null);
    }
}
