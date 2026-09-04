package com.urigym.domain.ranking;

import com.urigym.domain.review.Review;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scores review quality without any external service: a gym is rated higher when its
 * reviews are consistently positive (high mean, low spread) and are backed by written
 * content rather than bare star clicks.
 */
@Component
public class HeuristicReviewQualityScorer implements ReviewQualityScorer {

    private static final int SUBSTANTIVE_CONTENT_LENGTH = 20;

    @Override
    public double score(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return 0.0;
        }

        double mean = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        double variance = reviews.stream()
                .mapToDouble(review -> Math.pow(review.getRating() - mean, 2))
                .average()
                .orElse(0);

        double meanScore = (mean - 1) / 4.0;
        // A 5-point scale caps standard deviation at 2, so divide by 2 to normalise.
        double consistencyScore = 1 - Math.min(Math.sqrt(variance) / 2.0, 1.0);
        double substanceScore = (double) reviews.stream()
                .filter(review -> review.getContent() != null
                        && review.getContent().trim().length() >= SUBSTANTIVE_CONTENT_LENGTH)
                .count() / reviews.size();

        return 0.6 * meanScore + 0.2 * consistencyScore + 0.2 * substanceScore;
    }
}
