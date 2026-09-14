package com.urigym.domain.chat.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

    @NotBlank(message = "메시지 내용을 입력해주세요.")
    @Size(max = 1000, message = "메시지는 1000자 이내로 입력해주세요.")
    private String content;
}
