package com.urigym.domain.membershipplan;

import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.domain.gym.Gym;
import com.urigym.domain.membershipplan.entity.MembershipPlanRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;

    public List<MembershipPlan> getPlans(UUID gymId) {
        return membershipPlanRepository.findByGymIdOrderByPriceAsc(gymId);
    }

    @Transactional
    public MembershipPlan create(Gym gym, MembershipPlanRequest request) {
        return membershipPlanRepository.save(MembershipPlan.builder()
                .gym(gym)
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .build());
    }

    @Transactional
    public MembershipPlan update(UUID planId, UUID gymId, MembershipPlanRequest request) {
        MembershipPlan plan = getOwnedPlan(planId, gymId);
        plan.setName(request.getName());
        plan.setPrice(request.getPrice());
        plan.setDescription(request.getDescription());
        return membershipPlanRepository.save(plan);
    }

    @Transactional
    public void delete(UUID planId, UUID gymId) {
        membershipPlanRepository.delete(getOwnedPlan(planId, gymId));
    }

    private MembershipPlan getOwnedPlan(UUID planId, UUID gymId) {
        MembershipPlan plan = membershipPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with id: " + planId));
        if (!plan.getGym().getId().equals(gymId)) {
            throw new IllegalArgumentException("해당 체육관의 회원권이 아닙니다.");
        }
        return plan;
    }
}
