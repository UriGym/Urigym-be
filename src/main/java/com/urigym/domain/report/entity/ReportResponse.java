package com.urigym.domain.report.entity;

import com.urigym.domain.report.Report;
import com.urigym.domain.report.ReportCategory;
import com.urigym.domain.report.ReportStatus;
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
public class ReportResponse {

    private UUID id;
    private UUID reporterId;
    private String reporterName;
    private UUID gymId;
    private String gymName;
    private ReportCategory category;
    private String title;
    private String content;
    private ReportStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getId())
                .reporterName(report.getReporter().getFullName())
                .gymId(report.getGym() != null ? report.getGym().getId() : null)
                .gymName(report.getGym() != null ? report.getGym().getName() : null)
                .category(report.getCategory())
                .title(report.getTitle())
                .content(report.getContent())
                .status(report.getStatus())
                .adminNote(report.getAdminNote())
                .createdAt(report.getCreatedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
}
