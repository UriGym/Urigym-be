package com.urigym.domain.ranking;

import com.urigym.domain.gym.Gym;
import com.urigym.domain.gym.GymService;
import com.urigym.domain.review.Review;
import com.urigym.domain.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ranks gyms for the "AI 추천" surface by blending review quality, popularity,
 * price competitiveness and a penalty for accumulated reports.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GymRankingService {

    private static final double QUALITY_WEIGHT = 0.45;
    private static final double POPULARITY_WEIGHT = 0.20;
    private static final double PRICE_WEIGHT = 0.20;
    private static final double REPORT_PENALTY_WEIGHT = 0.15;

    /** Reports needed to zero out the report component of a gym's score. */
    private static final double REPORT_SATURATION = 10.0;

    private final GymService gymService;
    private final ReviewRepository reviewRepository;
    private final ReviewQualityScorer reviewQualityScorer;

    public List<RankedGym> getRankedGyms(int limit) {
        List<Gym> gyms = gymService.getAllVisibleGyms();
        if (gyms.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<Review>> reviewsByGym = reviewRepository
                .findByGymIdIn(gyms.stream().map(Gym::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(review -> review.getGym().getId()));

        int maxReviewCount = gyms.stream().mapToInt(Gym::getReviewCount).max().orElse(0);
        List<Integer> prices = gyms.stream().map(Gym::getPriceMin).filter(java.util.Objects::nonNull).toList();
        int minPrice = prices.stream().mapToInt(Integer::intValue).min().orElse(0);
        int maxPrice = prices.stream().mapToInt(Integer::intValue).max().orElse(0);

        return gyms.stream()
                .map(gym -> new RankedGym(
                        gym,
                        score(gym, reviewsByGym.getOrDefault(gym.getId(), List.of()), maxReviewCount, minPrice, maxPrice)
                ))
                .sorted(Comparator.comparingDouble(RankedGym::score).reversed())
                .limit(limit)
                .toList();
    }

    private double score(Gym gym, List<Review> reviews, int maxReviewCount, int minPrice, int maxPrice) {
        double quality = reviewQualityScorer.score(reviews);
        double popularity = maxReviewCount == 0 ? 0 : (double) gym.getReviewCount() / maxReviewCount;
        double price = priceScore(gym, minPrice, maxPrice);
        double reportHealth = 1 - Math.min(gym.getReportCount() / REPORT_SATURATION, 1.0);

        return QUALITY_WEIGHT * quality
                + POPULARITY_WEIGHT * popularity
                + PRICE_WEIGHT * price
                + REPORT_PENALTY_WEIGHT * reportHealth;
    }

    /** Cheaper gyms score higher; gyms with no listed price sit in the middle. */
    private double priceScore(Gym gym, int minPrice, int maxPrice) {
        if (gym.getPriceMin() == null || maxPrice == minPrice) {
            return 0.5;
        }
        return 1 - ((double) (gym.getPriceMin() - minPrice) / (maxPrice - minPrice));
    }

    public record RankedGym(Gym gym, double score) {
    }
}
