package com.urigym.domain.message;

import com.urigym.domain.gym.Gym;
import com.urigym.domain.member.GymMember;
import com.urigym.domain.member.GymMemberService;
import com.urigym.domain.message.entity.GroupMessageRequest;
import com.urigym.domain.notification.NotificationService;
import com.urigym.domain.notification.NotificationType;
import com.urigym.domain.user.User;
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
public class GroupMessageService {

    private final GroupMessageRepository groupMessageRepository;
    private final GymMemberService gymMemberService;
    private final NotificationService notificationService;

    public Page<GroupMessage> getMessages(UUID gymId, Pageable pageable) {
        return groupMessageRepository.findByGymIdOrderByCreatedAtDesc(gymId, pageable);
    }

    @Transactional
    public GroupMessage send(Gym gym, User sender, GroupMessageRequest request) {
        List<User> recipients = resolveRecipients(gym.getId(), request);
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("발송 대상이 없습니다.");
        }

        notificationService.notifyAll(
                recipients,
                NotificationType.MESSAGE,
                request.getTitle(),
                request.getContent(),
                gym.getId()
        );

        return groupMessageRepository.save(GroupMessage.builder()
                .gym(gym)
                .sender(sender)
                .title(request.getTitle())
                .content(request.getContent())
                .targetType(request.getTargetType())
                .recipientCount(recipients.size())
                .build());
    }

    private List<User> resolveRecipients(UUID gymId, GroupMessageRequest request) {
        if (request.getTargetType() == MessageTarget.ALL) {
            return gymMemberService.getMembers(gymId).stream().map(GymMember::getUser).toList();
        }

        if (request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
            throw new IllegalArgumentException("발송할 관원을 선택해주세요.");
        }

        return gymMemberService.getMembersByIds(request.getMemberIds()).stream()
                .filter(member -> member.getGym().getId().equals(gymId))
                .map(GymMember::getUser)
                .toList();
    }
}
