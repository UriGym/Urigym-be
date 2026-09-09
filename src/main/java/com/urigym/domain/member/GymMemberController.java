package com.urigym.domain.member;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.gym.entity.GymResponse;
import com.urigym.domain.member.entity.MyMembershipResponse;
import com.urigym.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
@Tag(name = "Membership", description = "내가 등록된 체육관 API")
public class GymMemberController {

    private final GymMemberRepository gymMemberRepository;
    private final GymMemberService gymMemberService;

    @GetMapping("/my-gyms")
    @Operation(summary = "내가 등록된 체육관 목록", description = "출석 체크가 가능한, 본인이 등록된 체육관을 조회합니다.")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getMyGyms(@AuthenticationPrincipal User user) {
        List<GymResponse> gyms = gymMemberRepository.findByUserIdAndStatus(user.getId(), "ACTIVE").stream()
                .map(member -> GymResponse.from(member.getGym()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(gyms));
    }

    @GetMapping("/mine")
    @Operation(summary = "내 회원권 목록", description = "등록된 체육관, 체육관별 출석 횟수, 가입일을 함께 조회합니다.")
    public ResponseEntity<ApiResponse<List<MyMembershipResponse>>> getMyMemberships(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(ApiResponse.success(gymMemberService.getMyMemberships(user.getId())));
    }

    @DeleteMapping("/mine/{membershipId}")
    @Operation(summary = "회원권 해지", description = "본인의 회원권을 해지합니다. 이후 해당 체육관 출석 체크는 불가능합니다.")
    public ResponseEntity<ApiResponse<Void>> cancelMyMembership(
            @AuthenticationPrincipal User user,
            @PathVariable UUID membershipId
    ) {
        gymMemberService.cancelOwnMembership(membershipId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("회원권이 해지되었습니다.", null));
    }
}
