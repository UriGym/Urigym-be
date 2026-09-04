package com.urigym.domain.report;

import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.domain.gym.Gym;
import com.urigym.domain.gym.GymService;
import com.urigym.domain.report.entity.ReportCreateRequest;
import com.urigym.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final GymService gymService;

    public Page<Report> getReportsByReporter(UUID reporterId, Pageable pageable) {
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(reporterId, pageable);
    }

    public Page<Report> getReports(ReportStatus status, Pageable pageable) {
        if (status == null) {
            return reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    public Report getReportById(UUID id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
    }

    @Transactional
    public Report create(User reporter, ReportCreateRequest request) {
        Gym gym = request.getGymId() != null ? gymService.getGymById(request.getGymId()) : null;

        Report report = reportRepository.save(Report.builder()
                .reporter(reporter)
                .gym(gym)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .build());

        if (gym != null) {
            gymService.incrementReportCount(gym.getId());
        }

        return report;
    }

    @Transactional
    public Report updateStatus(UUID reportId, ReportStatus status, String adminNote) {
        Report report = getReportById(reportId);
        report.setStatus(status);
        report.setAdminNote(adminNote);
        report.setResolvedAt(status == ReportStatus.OPEN ? null : LocalDateTime.now());
        return reportRepository.save(report);
    }
}
