package com.urigym.domain.favorite;

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
public class FavoriteService {

    private final GymFavoriteRepository gymFavoriteRepository;
    private final GymService gymService;

    public boolean isFavorited(UUID gymId, UUID userId) {
        return gymFavoriteRepository.existsByUserIdAndGymId(userId, gymId);
    }

    public Page<GymFavorite> getMyFavorites(UUID userId, Pageable pageable) {
        return gymFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional
    public boolean toggleFavorite(UUID gymId, User user) {
        Gym gym = gymService.getGymById(gymId);

        boolean nowFavorited = gymFavoriteRepository.findByUserIdAndGymId(user.getId(), gymId)
                .map(existing -> {
                    gymFavoriteRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    gymFavoriteRepository.save(GymFavorite.builder().user(user).gym(gym).build());
                    return true;
                });

        gymService.updateFavoriteCount(gymId, gymFavoriteRepository.countByGymId(gymId));
        return nowFavorited;
    }
}
