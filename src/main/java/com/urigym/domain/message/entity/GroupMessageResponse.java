package com.urigym.domain.message.entity;

import com.urigym.domain.message.GroupMessage;
import com.urigym.domain.message.MessageTarget;
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
public class GroupMessageResponse {

    private UUID id;
    private UUID gymId;
    private String title;
    private String content;
    private MessageTarget targetType;
    private Integer recipientCount;
    private LocalDateTime createdAt;

    public static GroupMessageResponse from(GroupMessage message) {
        return GroupMessageResponse.builder()
                .id(message.getId())
                .gymId(message.getGym().getId())
                .title(message.getTitle())
                .content(message.getContent())
                .targetType(message.getTargetType())
                .recipientCount(message.getRecipientCount())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
