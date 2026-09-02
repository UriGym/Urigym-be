package com.urigym.domain.announcement;

import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.domain.gym.Gym;
import com.urigym.domain.member.GymMember;
import com.urigym.domain.member.GymMemberService;
import com.urigym.domain.notification.NotificationService;
import com.urigym.domain.notification.NotificationType;
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
    private final GymMemberService gymMemberService;
    private final NotificationService notificationService;

    public Page<Announcement> getAnnouncementsByGym(UUID gymId, Pageable pageable) {
        return announcementRepository.findByGymIdOrderByCreatedAtDesc(gymId, pageable);
    }

    public Announcement getAnnouncementById(UUID id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));
    }

    /** Posting an announcement notifies every enrolled member of the gym. */
    @Transactional
    public Announcement createAnnouncement(Gym gym, String title, String content) {
        Announcement announcement = announcementRepository.save(Announcement.builder()
                .gym(gym)
                .title(title)
                .content(content)
                .build());

        notificationService.notifyAll(
                gymMemberService.getMembers(gym.getId()).stream().map(GymMember::getUser).toList(),
                NotificationType.ANNOUNCEMENT,
                "[" + gym.getName() + "] " + title,
                content,
                gym.getId()
        );

        return announcement;
    }

    @Transactional
    public Announcement updateAnnouncement(UUID id, UUID gymId, String title, String content) {
        Announcement announcement = getAnnouncementById(id);
        if (!announcement.getGym().getId().equals(gymId)) {
            throw new IllegalArgumentException("해당 체육관의 공지가 아닙니다.");
        }
        announcement.setTitle(title);
        announcement.setContent(content);
        return announcementRepository.save(announcement);
    }

    @Transactional
    public void deleteAnnouncement(UUID id, UUID gymId) {
        Announcement announcement = getAnnouncementById(id);
        if (!announcement.getGym().getId().equals(gymId)) {
            throw new IllegalArgumentException("해당 체육관의 공지가 아닙니다.");
        }
        announcementRepository.delete(announcement);
    }
}
