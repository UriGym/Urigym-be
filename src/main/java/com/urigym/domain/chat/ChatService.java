package com.urigym.domain.chat;

import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.domain.chat.entity.ChatRoomResponse;
import com.urigym.domain.gym.Gym;
import com.urigym.domain.gym.GymService;
import com.urigym.domain.notification.NotificationService;
import com.urigym.domain.notification.NotificationType;
import com.urigym.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GymService gymService;
    private final NotificationService notificationService;

    @Transactional
    public ChatRoom createOrGetRoom(UUID gymId, User currentUser) {
        Gym gym = gymService.getGymById(gymId);

        if (gym.getOwner() == null) {
            throw new IllegalArgumentException("아직 관장이 등록되지 않은 체육관에는 채팅 문의를 보낼 수 없습니다.");
        }
        if (gym.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("본인 체육관에는 문의할 수 없습니다.");
        }

        // ponytail: a race between two concurrent find-or-create calls on the same (gym, inquirer)
        // pair (e.g. a double-click) surfaces as the shared GlobalExceptionHandler's generic
        // data-integrity message rather than a clean "room already exists" response. Not fixed
        // with a same-transaction try/catch: on Postgres a failed insert poisons the rest of the
        // transaction, so a naive catch-and-refetch would trade one confusing error for a worse
        // one (500 instead of 409) — and this project's H2 local profile wouldn't reproduce that
        // to catch it in testing. A correct fix needs the retry in a REQUIRES_NEW transaction;
        // low priority given the FE already disables the button on the first click. Upgrade path:
        // small @Transactional(REQUIRES_NEW) helper bean around the insert if this proves to matter.
        return chatRoomRepository.findByGymIdAndInquirerId(gymId, currentUser.getId())
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder()
                        .gym(gym)
                        .inquirer(currentUser)
                        .lastMessageAt(LocalDateTime.now())
                        .build()));
    }

    public List<ChatRoomResponse> getRoomsForCurrentUser(User currentUser) {
        List<ChatRoom> rooms = chatRoomRepository.findByGymOwnerIdOrInquirerId(currentUser.getId());
        if (rooms.isEmpty()) {
            return List.of();
        }
        List<UUID> roomIds = rooms.stream().map(ChatRoom::getId).toList();

        Map<UUID, ChatMessage> lastMessages = new HashMap<>();
        chatMessageRepository.findLatestMessagesByRoomIds(roomIds)
                .forEach(message -> lastMessages.put(message.getRoom().getId(), message));

        Map<UUID, Long> unreadCounts = new HashMap<>();
        chatMessageRepository.countUnreadByRoomIds(roomIds, currentUser.getId())
                .forEach(row -> unreadCounts.put((UUID) row[0], (Long) row[1]));

        return rooms.stream()
                .map(room -> ChatRoomResponse.from(
                        room,
                        currentUser,
                        lastMessages.get(room.getId()),
                        unreadCounts.getOrDefault(room.getId(), 0L)))
                .toList();
    }

    /** Side effect: marks the counterpart's unread messages in this room as read. */
    @Transactional
    public List<ChatMessage> getMessages(UUID roomId, User currentUser) {
        ChatRoom room = getRoomOrThrow(roomId);
        getOtherParty(room, currentUser); // throws 403 if currentUser isn't a participant

        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
        messages.stream()
                .filter(message -> !message.getSender().getId().equals(currentUser.getId()))
                .filter(message -> !Boolean.TRUE.equals(message.getIsRead()))
                .forEach(message -> message.setIsRead(true));
        return messages;
    }

    @Transactional
    public ChatMessage sendMessage(UUID roomId, User currentUser, String content) {
        ChatRoom room = getRoomOrThrow(roomId);
        User recipient = getOtherParty(room, currentUser);

        // Captured explicitly rather than read back from message.getCreatedAt(): that field is
        // populated by Hibernate's @CreationTimestamp generator at flush time, which hasn't run
        // yet here — reading it now would still be null and silently null out lastMessageAt.
        LocalDateTime now = LocalDateTime.now();

        ChatMessage message = chatMessageRepository.save(ChatMessage.builder()
                .room(room)
                .sender(currentUser)
                .content(content)
                .build());

        room.setLastMessageAt(now);
        chatRoomRepository.save(room);

        boolean senderIsInquirer = currentUser.getId().equals(room.getInquirer().getId());
        String title = senderIsInquirer
                ? "[" + room.getGym().getName() + "] 문의자로부터 새 메시지가 도착했습니다."
                : "[" + room.getGym().getName() + "] 관장님으로부터 답변이 도착했습니다.";
        notificationService.notify(recipient, NotificationType.MESSAGE, title, content, room.getGym().getId());

        return message;
    }

    private ChatRoom getRoomOrThrow(UUID roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방을 찾을 수 없습니다: " + roomId));
    }

    /** Returns the other side of the 1:1 room, or throws 403 if {@code currentUser} isn't a participant. */
    private User getOtherParty(ChatRoom room, User currentUser) {
        User owner = room.getGym().getOwner();
        User inquirer = room.getInquirer();

        if (currentUser.getId().equals(inquirer.getId())) {
            return owner;
        }
        if (owner != null && currentUser.getId().equals(owner.getId())) {
            return inquirer;
        }
        throw new AccessDeniedException("본인이 참여한 채팅방만 이용할 수 있습니다.");
    }
}
