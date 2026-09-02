package com.urigym.domain.report;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.report.entity.ReportCreateRequest;
import com.urigym.domain.report.entity.ReportResponse;
import com.urigym.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Report", description = "신고 / 문의 API")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "신고 / 문의 등록", description = "체육관 신고 또는 일반 문의를 등록합니다.")
    public ResponseEntity<ApiResponse<ReportResponse>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        Report report = reportService.create(user, request);
        return ResponseEntity.ok(ApiResponse.success("접수되었습니다.", ReportResponse.from(report)));
    }

    @GetMapping("/me")
    @Operation(summary = "내 신고 / 문의 내역", description = "본인이 등록한 신고와 문의를 조회합니다.")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getMyReports(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ReportResponse> reports = reportService.getReportsByReporter(user.getId(), pageable)
                .map(ReportResponse::from);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }
}
