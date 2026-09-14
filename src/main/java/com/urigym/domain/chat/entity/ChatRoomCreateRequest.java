package com.urigym.domain.chat.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreateRequest {

    @NotNull(message = "체육관을 선택해주세요.")
    private UUID gymId;
}
