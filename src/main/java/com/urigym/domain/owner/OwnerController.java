package com.urigym.domain.owner;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.announcement.AnnouncementService;
import com.urigym.domain.announcement.entity.AnnouncementRequest;
import com.urigym.domain.announcement.entity.AnnouncementResponse;
import com.urigym.domain.attendance.AttendanceService;
import com.urigym.domain.attendance.entity.AttendanceResponse;
import com.urigym.domain.event.EventService;
import com.urigym.domain.event.entity.EventRequest;
import com.urigym.domain.event.entity.EventResponse;
import com.urigym.domain.gym.Gym;
import com.urigym.domain.gym.GymService;
import com.urigym.domain.gym.entity.GymOwnerRequest;
import com.urigym.domain.gym.entity.GymResponse;
import com.urigym.domain.member.GymMemberService;
import com.urigym.domain.member.entity.GymMemberRequest;
import com.urigym.domain.member.entity.GymMemberResponse;
import com.urigym.domain.membershipplan.MembershipPlanService;
import com.urigym.domain.membershipplan.entity.MembershipPlanRequest;
import com.urigym.domain.membershipplan.entity.MembershipPlanResponse;
import com.urigym.domain.message.GroupMessageService;
import com.urigym.domain.message.entity.GroupMessageRequest;
import com.urigym.domain.message.entity.GroupMessageResponse;
import com.urigym.domain.review.ReviewService;
import com.urigym.domain.review.entity.ReviewResponse;
import com.urigym.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Owner-only surface. Every gym-scoped call resolves the gym through
 * {@link GymService#getOwnedGym}, so an owner can never reach another owner's gym.
 */
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "Owner", description = "관장 전용 API")
public class OwnerController {

    private final GymService gymService;
    private final GymMemberService gymMemberService;
    private final AnnouncementService announcementService;
    private final EventService eventService;
    private final GroupMessageService groupMessageService;
    private final ReviewService reviewService;
    private final AttendanceService attendanceService;
    private final MembershipPlanService membershipPlanService;

    // --- Gyms ---

    @GetMapping("/gyms")
    @Operation(summary = "내 체육관 목록", description = "관장이 등록한 모든 체육관을 조회합니다.")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getMyGyms(@AuthenticationPrincipal User owner) {
        List<GymResponse> gyms = gymService.getGymsByOwner(owner.getId()).stream()
                .map(GymResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(gyms));
    }

    @PostMapping("/gyms")
    @Operation(summary = "체육관 등록", description = "새 체육관을 등록합니다. 가격 등록은 필수입니다.")
    public ResponseEntity<ApiResponse<GymResponse>> createGym(
            @AuthenticationPrincipal User owner,
            @Valid @RequestBody GymOwnerRequest request
    ) {
        Gym gym = gymService.createGym(owner, request);
        return ResponseEntity.ok(ApiResponse.success("체육관이 등록되었습니다.", GymResponse.from(gym)));
    }

    @PutMapping("/gyms/{gymId}")
    @Operation(summary = "체육관 수정", description = "본인이 등록한 체육관 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<GymResponse>> updateGym(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @Valid @RequestBody GymOwnerRequest request
    ) {
        Gym gym = gymService.updateGym(gymId, owner.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("수정되었습니다.", GymResponse.from(gym)));
    }

    @DeleteMapping("/gyms/{gymId}")
    @Operation(summary = "체육관 삭제", description = "본인이 등록한 체육관을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteGym(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId
    ) {
        gymService.deleteGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success("삭제되었습니다.", null));
    }

    // --- Membership plans ---

    @PostMapping("/gyms/{gymId}/membership-plans")
    @Operation(summary = "회원권 등록", description = "1개월/3개월/6개월 등 회원권을 등록합니다.")
    public ResponseEntity<ApiResponse<MembershipPlanResponse>> createMembershipPlan(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @Valid @RequestBody MembershipPlanRequest request
    ) {
        Gym gym = gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "회원권이 등록되었습니다.",
                MembershipPlanResponse.from(membershipPlanService.create(gym, request))
        ));
    }

    @PutMapping("/gyms/{gymId}/membership-plans/{planId}")
    @Operation(summary = "회원권 수정", description = "등록된 회원권의 이름/가격/설명을 수정합니다.")
    public ResponseEntity<ApiResponse<MembershipPlanResponse>> updateMembershipPlan(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @PathVariable UUID planId,
            @Valid @RequestBody MembershipPlanRequest request
    ) {
        gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "수정되었습니다.",
                MembershipPlanResponse.from(membershipPlanService.update(planId, gymId, request))
        ));
    }

    @DeleteMapping("/gyms/{gymId}/membership-plans/{planId}")
    @Operation(summary = "회원권 삭제", description = "등록된 회원권을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteMembershipPlan(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @PathVariable UUID planId
    ) {
        gymService.getOwnedGym(gymId, owner.getId());
        membershipPlanService.delete(planId, gymId);
        return ResponseEntity.ok(ApiResponse.success("삭제되었습니다.", null));
    }

    // --- Members ---

    @GetMapping("/gyms/{gymId}/members")
    @Operation(summary = "관원 목록", description = "체육관의 관원 목록을 출석률과 함께 조회합니다.")
    public ResponseEntity<ApiResponse<List<GymMemberResponse>>> getMembers(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId
    ) {
        gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(gymMemberService.getMembersWithStats(gymId)));
    }

    @GetMapping("/gyms/{gymId}/members/absent")
    @Operation(summary = "장기 미출석 관원", description = "지정한 일수 이상 출석하지 않은 관원을 조회합니다.")
    public ResponseEntity<ApiResponse<List<GymMemberResponse>>> getAbsentMembers(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "7") int days
    ) {
        gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(gymMemberService.getAbsentMembers(gymId, days)));
    }

    @PostMapping("/gyms/{gymId}/members")
    @Operation(summary = "관원 등록", description = "이미 가입한 사용자를 이메일로 관원 등록합니다.")
    public ResponseEntity<ApiResponse<GymMemberResponse>> addMember(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @Valid @RequestBody GymMemberRequest request
    ) {
        Gym gym = gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "관원이 등록되었습니다.",
                GymMemberResponse.from(gymMemberService.addMember(gym, request))
        ));
    }

    @PutMapping("/gyms/{gymId}/members/{memberId}")
    @Operation(summary = "관원 수정", description = "관원의 상태와 만료일을 수정합니다.")
    public ResponseEntity<ApiResponse<GymMemberResponse>> updateMember(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @PathVariable UUID memberId,
            @RequestBody GymMemberRequest request
    ) {
        gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "수정되었습니다.",
                GymMemberResponse.from(gymMemberService.updateMember(memberId, gymId, request))
        ));
    }

    @DeleteMapping("/gyms/{gymId}/members/{memberId}")
    @Operation(summary = "관원 삭제", description = "관원을 체육관에서 제외합니다.")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @PathVariable UUID memberId
    ) {
        Gym gym = gymService.getOwnedGym(gymId, owner.getId());
        gymMemberService.removeMember(memberId, gymId, gym);
        return ResponseEntity.ok(ApiResponse.success("삭제되었습니다.", null));
    }

    @PostMapping("/gyms/{gymId}/members/{memberId}/approve")
    @Operation(summary = "등록 신청 승인", description = "대기중인 등록 신청을 승인하고 정식 관원(ACTIVE)으로 전환합니다.")
    public ResponseEntity<ApiResponse<GymMemberResponse>> approveMember(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @PathVariable UUID memberId
    ) {
        Gym gym = gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "등록 신청을 승인했습니다.",
                GymMemberResponse.from(gymMemberService.approveMember(memberId, gymId, gym))
        ));
    }

    @PostMapping("/gyms/{gymId}/members/{memberId}/reject")
    @Operation(summary = "등록 신청 거절", description = "대기중인 등록 신청을 거절합니다.")
    public ResponseEntity<ApiResponse<Void>> rejectMember(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @PathVariable UUID memberId
    ) {
        Gym gym = gymService.getOwnedGym(gymId, owner.getId());
        gymMemberService.rejectMember(memberId, gymId, gym);
        return ResponseEntity.ok(ApiResponse.success("등록 신청을 거절했습니다.", null));
    }

    // --- Announcements ---

    @PostMapping("/gyms/{gymId}/announcements")
    @Operation(summary = "공지 등록", description = "공지를 등록하면 관원 전원에게 알림이 발송됩니다.")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> createAnnouncement(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @Valid @RequestBody AnnouncementRequest request
    ) {
        Gym gym = gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "공지가 등록되고 관원에게 알림이 발송되었습니다.",
                AnnouncementResponse.from(announcementService.createAnnouncement(gym, request.getTitle(), request.getContent()))
        ));
    }

    @PutMapping("/gyms/{gymId}/announcements/{announcementId}")
    @Operation(summary = "공지 수정", description = "등록된 공지 내용을 수정합니다.")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> updateAnnouncement(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @PathVariable UUID announcementId,
            @Valid @RequestBody AnnouncementRequest request
    ) {
        gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "수정되었습니다.",
                AnnouncementResponse.from(announcementService.updateAnnouncement(
                        announcementId, gymId, request.getTitle(), request.getContent()))
        ));
    }

    @DeleteMapping("/gyms/{gymId}/announcements/{announcementId}")
    @Operation(summary = "공지 삭제", description = "등록된 공지를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @PathVariable UUID announcementId
    ) {
        gymService.getOwnedGym(gymId, owner.getId());
        announcementService.deleteAnnouncement(announcementId, gymId);
        return ResponseEntity.ok(ApiResponse.success("삭제되었습니다.", null));
    }

    // --- Events ---

    @PostMapping("/gyms/{gymId}/events")
    @Operation(summary = "이벤트 등록", description = "심사 등 체육관 이벤트를 등록합니다.")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @Valid @RequestBody EventRequest request
    ) {
        Gym gym = gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "이벤트가 등록되었습니다.",
                EventResponse.from(eventService.create(gym, request))
        ));
    }

    @DeleteMapping("/gyms/{gymId}/events/{eventId}")
    @Operation(summary = "이벤트 삭제", description = "등록된 이벤트를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @PathVariable UUID eventId
    ) {
        gymService.getOwnedGym(gymId, owner.getId());
        eventService.delete(eventId);
        return ResponseEntity.ok(ApiResponse.success("삭제되었습니다.", null));
    }

    // --- Group messages ---

    @PostMapping("/gyms/{gymId}/messages")
    @Operation(summary = "단체 메시지 발송", description = "관원 전체 또는 선택한 관원에게 메시지를 발송합니다.")
    public ResponseEntity<ApiResponse<GroupMessageResponse>> sendGroupMessage(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @Valid @RequestBody GroupMessageRequest request
    ) {
        Gym gym = gymService.getOwnedGym(gymId, owner.getId());
        GroupMessageResponse response = GroupMessageResponse.from(groupMessageService.send(gym, owner, request));
        return ResponseEntity.ok(ApiResponse.success(
                response.getRecipientCount() + "명에게 발송되었습니다.",
                response
        ));
    }

    @GetMapping("/gyms/{gymId}/messages")
    @Operation(summary = "단체 메시지 발송 이력", description = "체육관의 단체 메시지 발송 이력을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<GroupMessageResponse>>> getGroupMessages(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(
                groupMessageService.getMessages(gymId, pageable).map(GroupMessageResponse::from)
        ));
    }

    // --- Read-only insight ---

    @GetMapping("/gyms/{gymId}/reviews")
    @Operation(summary = "체육관 리뷰 조회", description = "관장은 리뷰를 열람만 할 수 있고 수정할 수 없습니다.")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getReviewsByGym(gymId, pageable).map(ReviewResponse::from)
        ));
    }

    @GetMapping("/gyms/{gymId}/attendances")
    @Operation(summary = "체육관 출석 현황", description = "체육관의 최근 출석 기록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getAttendances(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getAttendancesByGym(gymId, pageable).map(AttendanceResponse::from)
        ));
    }

    @GetMapping("/gyms/{gymId}/stats")
    @Operation(summary = "체육관 요약 통계", description = "관원 수, 오늘 출석, 평점, 장기 미출석 인원을 조회합니다.")
    public ResponseEntity<ApiResponse<OwnerGymStats>> getStats(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "7") int absentDays
    ) {
        Gym gym = gymService.getOwnedGym(gymId, owner.getId());
        return ResponseEntity.ok(ApiResponse.success(new OwnerGymStats(
                gym.getMemberCount(),
                attendanceService.countTodayAttendances(gymId),
                gym.getRating(),
                gymMemberService.getAbsentMembers(gymId, absentDays).size()
        )));
    }

    public record OwnerGymStats(
            int memberCount,
            long todayAttendance,
            java.math.BigDecimal rating,
            int absentMemberCount
    ) {
    }
}
