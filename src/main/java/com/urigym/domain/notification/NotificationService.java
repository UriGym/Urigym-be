package com.urigym.domain.notification;

import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Page<Notification> getNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long countUnread(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다.");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /** SYSTEM notifications (approvals etc.) are never user-silenceable — only
     *  ANNOUNCEMENT and MESSAGE respect the recipient's notification settings. */
    @Transactional
    public void notify(User user, NotificationType type, String title, String body, UUID relatedGymId) {
        if (!wantsNotification(user, type)) {
            return;
        }
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .relatedGymId(relatedGymId)
                .build());
    }

    @Transactional
    public int notifyAll(Collection<User> users, NotificationType type, String title, String body, UUID relatedGymId) {
        List<Notification> notifications = users.stream()
                .filter(user -> wantsNotification(user, type))
                .map(user -> Notification.builder()
                        .user(user)
                        .type(type)
                        .title(title)
                        .body(body)
                        .relatedGymId(relatedGymId)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    private boolean wantsNotification(User user, NotificationType type) {
        return switch (type) {
            case ANNOUNCEMENT -> Boolean.TRUE.equals(user.getNotifyAnnouncements());
            case MESSAGE -> Boolean.TRUE.equals(user.getNotifyMessages());
            case SYSTEM -> true;
        };
    }
}
