package com.urigym.domain.ownerapplication;

import com.urigym.common.exception.DuplicateResourceException;
import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.domain.notification.NotificationService;
import com.urigym.domain.notification.NotificationType;
import com.urigym.domain.ownerapplication.entity.OwnerApplicationRequest;
import com.urigym.domain.user.AppRole;
import com.urigym.domain.user.User;
import com.urigym.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerApplicationService {

    private final OwnerApplicationRepository ownerApplicationRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    public Optional<OwnerApplication> getLatestApplication(UUID userId) {
        return ownerApplicationRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<OwnerApplication> getApplications(ApplicationStatus status, Pageable pageable) {
        if (status == null) {
            return ownerApplicationRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return ownerApplicationRepository.findByStatusOrderByCreatedAtAsc(status, pageable);
    }

    @Transactional
    public OwnerApplication apply(User user, OwnerApplicationRequest request) {
        if (user.getRole() == AppRole.OWNER) {
            throw new DuplicateResourceException("이미 관장 권한을 보유하고 있습니다.");
        }
        if (ownerApplicationRepository.existsByUserIdAndStatus(user.getId(), ApplicationStatus.PENDING)) {
            throw new DuplicateResourceException("이미 심사 대기 중인 신청이 있습니다.");
        }

        return ownerApplicationRepository.save(OwnerApplication.builder()
                .user(user)
                .businessRegImageUrl(request.getBusinessRegImageUrl())
                .licenseImageUrl(request.getLicenseImageUrl())
                .businessNumber(request.getBusinessNumber())
                .build());
    }

    /**
     * Approval is only ever reached through this admin-triggered path — there is no
     * automatic promotion to OWNER anywhere in the codebase.
     */
    @Transactional
    public OwnerApplication review(UUID applicationId, boolean approve, String adminNote) {
        OwnerApplication application = ownerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner application not found with id: " + applicationId));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 신청입니다.");
        }

        application.setStatus(approve ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED);
        application.setAdminNote(adminNote);
        application.setReviewedAt(LocalDateTime.now());
        ownerApplicationRepository.save(application);

        if (approve) {
            userService.changeRole(application.getUser().getId(), AppRole.OWNER);
        }

        notificationService.notify(
                application.getUser(),
                NotificationType.SYSTEM,
                approve ? "관장 등록이 승인되었습니다." : "관장 등록이 반려되었습니다.",
                adminNote,
                null
        );

        return application;
    }
}
