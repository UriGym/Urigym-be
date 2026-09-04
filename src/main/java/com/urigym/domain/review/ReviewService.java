package com.urigym.domain.review;

import com.urigym.common.exception.DuplicateResourceException;
import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.domain.gym.Gym;
import com.urigym.domain.gym.GymService;
import com.urigym.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final GymService gymService;

    public Page<Review> getReviewsByGym(UUID gymId, Pageable pageable) {
        return reviewRepository.findByGymIdOrderByCreatedAtDesc(gymId, pageable);
    }

    public Page<Review> getReviewsByUser(UUID userId, Pageable pageable) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Review getReviewById(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
    }

    @Transactional
    public Review createReview(UUID gymId, User user, int rating, String content) {
        Gym gym = gymService.getGymById(gymId);

        if (reviewRepository.existsByGymIdAndUserId(gymId, user.getId())) {
            throw new DuplicateResourceException("You have already reviewed this gym");
        }

        Review review = Review.builder()
                .gym(gym)
                .user(user)
                .rating(rating)
                .content(content)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update gym stats
        updateGymReviewStats(gymId);

        return savedReview;
    }

    @Transactional
    public Review updateReview(UUID reviewId, UUID userId, int rating, String content) {
        Review review = getReviewById(reviewId);

        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own review");
        }

        review.setRating(rating);
        review.setContent(content);

        Review updatedReview = reviewRepository.save(review);

        // Update gym stats
        updateGymReviewStats(review.getGym().getId());

        return updatedReview;
    }

    @Transactional
    public void deleteReview(UUID reviewId, UUID userId) {
        Review review = getReviewById(reviewId);

        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own review");
        }

        UUID gymId = review.getGym().getId();
        reviewRepository.delete(review);

        // Update gym stats
        updateGymReviewStats(gymId);
    }

    private void updateGymReviewStats(UUID gymId) {
        long count = reviewRepository.countByGymId(gymId);
        Double avgRating = reviewRepository.getAverageRatingByGymId(gymId);
        gymService.updateGymStats(gymId, (int) count, avgRating != null ? avgRating : 0.0);
    }
}
