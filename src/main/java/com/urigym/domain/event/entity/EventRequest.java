package com.urigym.domain.event.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    private String description;

    @NotNull(message = "이벤트 날짜를 선택해주세요.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;
}
