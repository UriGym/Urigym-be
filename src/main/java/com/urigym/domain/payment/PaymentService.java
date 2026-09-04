package com.urigym.domain.payment;

import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.domain.gym.Gym;
import com.urigym.domain.member.GymMember;
import com.urigym.domain.member.GymMemberRepository;
import com.urigym.domain.membershipplan.MembershipPlan;
import com.urigym.domain.membershipplan.MembershipPlanRepository;
import com.urigym.domain.payment.entity.PaymentConfirmRequest;
import com.urigym.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final MembershipOrderRepository orderRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final GymMemberRepository gymMemberRepository;
    private final TossPaymentClient tossPaymentClient;

    @Transactional
    public MembershipOrder createOrder(User user, UUID membershipPlanId) {
        MembershipPlan plan = membershipPlanRepository.findById(membershipPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with id: " + membershipPlanId));

        Gym gym = plan.getGym();
        if (gym.isSuspended()) {
            throw new IllegalArgumentException("현재 노출이 정지된 체육관은 이용권을 구매할 수 없습니다.");
        }

        return orderRepository.save(MembershipOrder.builder()
                .orderId(UUID.randomUUID().toString().replace("-", ""))
                .user(user)
                .gym(gym)
                .membershipPlan(plan)
                .orderName(gym.getName() + " - " + plan.getName())
                .amount(plan.getPrice())
                .build());
    }

    /**
     * Toss confirms the charge itself; this only records the result and grants
     * membership. The amount check guards against a tampered success-redirect URL
     * trying to confirm a different (larger) order at a smaller price — Toss would
     * reject a mismatched amount too, but failing fast here avoids the network round trip
     * and gives a clearer error.
     */
    @Transactional
    public MembershipOrder confirm(User user, PaymentConfirmRequest request) {
        MembershipOrder order = orderRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("본인의 주문만 결제할 수 있습니다.");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 주문입니다.");
        }
        if (!order.getAmount().equals(request.getAmount())) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        try {
            tossPaymentClient.confirm(request.getPaymentKey(), request.getOrderId(), request.getAmount());
        } catch (IllegalArgumentException e) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            throw e;
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaymentKey(request.getPaymentKey());
        order.setApprovedAt(LocalDateTime.now());
        orderRepository.save(order);

        grantMembership(order);
        return order;
    }

    private void grantMembership(MembershipOrder order) {
        Gym gym = order.getGym();
        User user = order.getUser();

        if (gymMemberRepository.existsByGymIdAndUserId(gym.getId(), user.getId())) {
            return;
        }

        gymMemberRepository.save(GymMember.builder().gym(gym).user(user).build());
        gym.setMemberCount((int) gymMemberRepository.countByGymId(gym.getId()));
    }
}
