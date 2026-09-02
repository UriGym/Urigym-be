package com.urigym.domain.gym;

import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.domain.gym.entity.GymOwnerRequest;
import com.urigym.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GymService {

    private final GymRepository gymRepository;

    public Page<Gym> getAllGyms(Pageable pageable) {
        return gymRepository.findAll(visible(), pageable);
    }

    public List<Gym> getAllVisibleGyms() {
        return gymRepository.findAll(visible());
    }

    /** Admin view — includes gyms currently suspended from public listings. */
    public Page<Gym> getAllGymsIncludingSuspended(Pageable pageable) {
        return gymRepository.findAll(pageable);
    }

    public Gym getGymById(UUID id) {
        return gymRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found with id: " + id));
    }

    public Page<Gym> getGymsByCategory(String category, Pageable pageable) {
        return gymRepository.findAll(visible().and(GymSpecifications.hasCategory(category)), pageable);
    }

    public Page<Gym> searchGyms(String keyword, Pageable pageable) {
        return gymRepository.findAll(visible().and(GymSpecifications.matchesKeyword(keyword)), pageable);
    }

    public List<Gym> getGymsByLocation(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        return gymRepository.findAll(
                visible().and(GymSpecifications.withinBounds(minLat, maxLat, minLng, maxLng)));
    }

    private Specification<Gym> visible() {
        return GymSpecifications.visible(LocalDateTime.now());
    }

    public List<Gym> getGymsByOwner(UUID ownerId) {
        return gymRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public Gym createGym(User owner, GymOwnerRequest request) {
        Gym gym = Gym.builder().owner(owner).build();
        apply(gym, request);
        return gymRepository.save(gym);
    }

    @Transactional
    public Gym updateGym(UUID id, UUID ownerId, GymOwnerRequest request) {
        Gym gym = getOwnedGym(id, ownerId);
        apply(gym, request);
        return gymRepository.save(gym);
    }

    @Transactional
    public void deleteGym(UUID id, UUID ownerId) {
        gymRepository.delete(getOwnedGym(id, ownerId));
    }

    private void apply(Gym gym, GymOwnerRequest request) {
        gym.setName(request.getName());
        gym.setCategory(request.getCategory());
        gym.setAddress(request.getAddress());
        gym.setDescription(request.getDescription());
        gym.setPhone(request.getPhone());
        gym.setImageUrl(request.getImageUrl());
        gym.setLat(request.getLat());
        gym.setLng(request.getLng());
        gym.setPriceMin(request.getPriceMin());
        gym.setPriceMax(request.getPriceMax());
        gym.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        if (request.getIsOpen() != null) {
            gym.setIsOpen(request.getIsOpen());
        }
    }

    @Transactional
    public void updateGymStats(UUID gymId, int reviewCount, Double avgRating) {
        Gym gym = getGymById(gymId);
        gym.setReviewCount(reviewCount);
        gym.setRating(java.math.BigDecimal.valueOf(avgRating));
        gymRepository.save(gym);
    }

    @Transactional
    public void incrementReportCount(UUID gymId) {
        Gym gym = getGymById(gymId);
        gym.setReportCount(gym.getReportCount() + 1);
        gymRepository.save(gym);
    }

    @Transactional
    public Gym suspend(UUID gymId, int days) {
        if (days < 1) {
            throw new IllegalArgumentException("정지 기간은 1일 이상이어야 합니다.");
        }
        Gym gym = getGymById(gymId);
        gym.setSuspendedUntil(LocalDateTime.now().plusDays(days));
        return gymRepository.save(gym);
    }

    @Transactional
    public Gym unsuspend(UUID gymId) {
        Gym gym = getGymById(gymId);
        gym.setSuspendedUntil(null);
        return gymRepository.save(gym);
    }

    public Gym getOwnedGym(UUID gymId, UUID ownerId) {
        Gym gym = getGymById(gymId);
        if (!gym.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("본인이 등록한 체육관만 관리할 수 있습니다.");
        }
        return gym;
    }
}
