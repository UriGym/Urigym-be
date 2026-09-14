package com.urigym.domain.chat.entity;

import com.urigym.domain.chat.ChatMessage;
import com.urigym.domain.chat.ChatRoom;
import com.urigym.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {

    private UUID id;
    private UUID gymId;
    private String gymName;
    private String counterpartName;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
    private LocalDateTime createdAt;

    /**
     * @param viewer      the requesting user — determines which side (inquirer/owner) is "the counterpart"
     * @param lastMessage most recent message in the room, or null if none sent yet
     * @param unreadCount messages sent by the counterpart that {@code viewer} hasn't read yet
     */
    public static ChatRoomResponse from(ChatRoom room, User viewer, ChatMessage lastMessage, long unreadCount) {
        User counterpart = viewer.getId().equals(room.getInquirer().getId())
                ? room.getGym().getOwner()
                : room.getInquirer();

        return ChatRoomResponse.builder()
                .id(room.getId())
                .gymId(room.getGym().getId())
                .gymName(room.getGym().getName())
                .counterpartName(counterpart.getFullName())
                .lastMessage(lastMessage == null ? null : lastMessage.getContent())
                .lastMessageAt(room.getLastMessageAt())
                .unreadCount(unreadCount)
                .createdAt(room.getCreatedAt())
                .build();
    }
}
