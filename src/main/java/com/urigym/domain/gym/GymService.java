package com.urigym.domain.gym;

import com.urigym.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GymService {

    private final GymRepository gymRepository;

    public Page<Gym> getAllGyms(Pageable pageable) {
        return gymRepository.findAll(pageable);
    }

    public Gym getGymById(UUID id) {
        return gymRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found with id: " + id));
    }

    public Page<Gym> getGymsByCategory(String category, Pageable pageable) {
        return gymRepository.findByCategory(category, pageable);
    }

    public Page<Gym> searchGyms(String keyword, Pageable pageable) {
        return gymRepository.searchByKeyword(keyword, pageable);
    }

    public List<Gym> getGymsByLocation(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        return gymRepository.findByLocationBounds(minLat, maxLat, minLng, maxLng);
    }

    public List<Gym> getGymsByOwner(UUID ownerId) {
        return gymRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public Gym createGym(Gym gym) {
        return gymRepository.save(gym);
    }

    @Transactional
    public Gym updateGym(UUID id, Gym gymDetails) {
        Gym gym = getGymById(id);

        gym.setName(gymDetails.getName());
        gym.setCategory(gymDetails.getCategory());
        gym.setAddress(gymDetails.getAddress());
        gym.setDescription(gymDetails.getDescription());
        gym.setPhone(gymDetails.getPhone());
        gym.setImageUrl(gymDetails.getImageUrl());
        gym.setIsOpen(gymDetails.getIsOpen());
        gym.setLat(gymDetails.getLat());
        gym.setLng(gymDetails.getLng());
        gym.setPriceMin(gymDetails.getPriceMin());
        gym.setPriceMax(gymDetails.getPriceMax());
        gym.setTags(gymDetails.getTags());

        return gymRepository.save(gym);
    }

    @Transactional
    public void deleteGym(UUID id) {
        Gym gym = getGymById(id);
        gymRepository.delete(gym);
    }

    @Transactional
    public void updateGymStats(UUID gymId, int reviewCount, Double avgRating) {
        Gym gym = getGymById(gymId);
        gym.setReviewCount(reviewCount);
        gym.setRating(java.math.BigDecimal.valueOf(avgRating));
        gymRepository.save(gym);
    }
}
