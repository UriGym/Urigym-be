package com.urigym.domain.ownerapplication.entity;

import com.urigym.domain.ownerapplication.ApplicationStatus;
import com.urigym.domain.ownerapplication.OwnerApplication;
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
public class OwnerApplicationResponse {

    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String businessRegImageUrl;
    private String licenseImageUrl;
    private String businessNumber;
    private ApplicationStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public static OwnerApplicationResponse from(OwnerApplication application) {
        return OwnerApplicationResponse.builder()
                .id(application.getId())
                .userId(application.getUser().getId())
                .userName(application.getUser().getFullName())
                .userEmail(application.getUser().getEmail())
                .userPhone(application.getUser().getPhone())
                .businessRegImageUrl(application.getBusinessRegImageUrl())
                .licenseImageUrl(application.getLicenseImageUrl())
                .businessNumber(application.getBusinessNumber())
                .status(application.getStatus())
                .adminNote(application.getAdminNote())
                .createdAt(application.getCreatedAt())
                .reviewedAt(application.getReviewedAt())
                .build();
    }
}
