package com.urigym.domain.ranking;

import com.urigym.domain.review.Review;

import java.util.List;

/**
 * Turns a gym's raw reviews into a 0..1 quality signal for {@link GymRankingService}.
 * <p>
 * The heuristic implementation is the default. To move to an LLM-backed analysis
 * later, add a second implementation annotated {@code @Primary} (or select it with
 * {@code @ConditionalOnProperty}) — no ranking or controller code needs to change.
 */
public interface ReviewQualityScorer {

    double score(List<Review> reviews);
}
