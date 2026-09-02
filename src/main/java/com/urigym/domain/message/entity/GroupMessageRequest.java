package com.urigym.domain.message.entity;

import com.urigym.domain.message.MessageTarget;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    @NotNull(message = "발송 대상을 선택해주세요.")
    private MessageTarget targetType;

    /** Gym member ids — required when targetType is SELECTED. */
    private List<UUID> memberIds;
}
