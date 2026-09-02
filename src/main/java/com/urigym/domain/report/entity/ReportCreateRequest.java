package com.urigym.domain.report.entity;

import com.urigym.domain.report.ReportCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateRequest {

    /** Optional — omit for a general inquiry. */
    private UUID gymId;

    @NotNull(message = "유형을 선택해주세요.")
    private ReportCategory category;

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
}
