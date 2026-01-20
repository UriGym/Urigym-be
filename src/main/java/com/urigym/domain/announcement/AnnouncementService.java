package com.urigym.domain.announcement;

import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.domain.gym.Gym;
import com.urigym.domain.gym.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final GymService gymService;

    public Page<Announcement> getAnnouncementsByGym(UUID gymId, Pageable pageable) {
        return announcementRepository.findByGymIdOrderByCreatedAtDesc(gymId, pageable);
    }

    public Announcement getAnnouncementById(UUID id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));
    }

    @Transactional
    public Announcement createAnnouncement(UUID gymId, String title, String content) {
        Gym gym = gymService.getGymById(gymId);

        Announcement announcement = Announcement.builder()
                .gym(gym)
                .title(title)
                .content(content)
                .build();

        return announcementRepository.save(announcement);
    }

    @Transactional
    public void deleteAnnouncement(UUID id) {
        Announcement announcement = getAnnouncementById(id);
        announcementRepository.delete(announcement);
    }
}
