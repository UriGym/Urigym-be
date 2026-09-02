package com.urigym.domain.admin.entity;

import com.urigym.domain.report.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportStatusRequest {

    @NotNull(message = "처리 상태를 선택해주세요.")
    private ReportStatus status;

    private String adminNote;
}
