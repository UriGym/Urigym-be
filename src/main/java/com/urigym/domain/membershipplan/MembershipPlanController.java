package com.urigym.domain.membershipplan;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.membershipplan.entity.MembershipPlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gyms/{gymId}/membership-plans")
@RequiredArgsConstructor
@Tag(name = "MembershipPlan", description = "체육관 회원권 API")
public class MembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    @GetMapping
    @Operation(summary = "회원권 목록 조회", description = "체육관이 등록한 회원권(기간/가격)을 가격순으로 조회합니다.")
    public ResponseEntity<ApiResponse<List<MembershipPlanResponse>>> getPlans(@PathVariable UUID gymId) {
        List<MembershipPlanResponse> plans = membershipPlanService.getPlans(gymId)
                .stream()
                .map(MembershipPlanResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(plans));
    }
}
