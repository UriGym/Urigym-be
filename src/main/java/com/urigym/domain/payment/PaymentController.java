package com.urigym.domain.payment;

import com.urigym.common.response.ApiResponse;
import com.urigym.domain.payment.entity.OrderCreateRequest;
import com.urigym.domain.payment.entity.OrderResponse;
import com.urigym.domain.payment.entity.PaymentConfirmRequest;
import com.urigym.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "이용권 결제 API (Toss Payments)")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    @Operation(summary = "주문 생성", description = "이용권을 선택해 결제 위젯에 넘길 주문을 생성합니다. 금액은 서버가 회원권 가격으로 고정합니다.")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        OrderResponse response = OrderResponse.from(paymentService.createOrder(user, request.getMembershipPlanId()));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/confirm")
    @Operation(summary = "결제 승인", description = "Toss 결제 성공 리다이렉트 파라미터로 결제를 승인하고 이용권을 활성화합니다.")
    public ResponseEntity<ApiResponse<OrderResponse>> confirm(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        OrderResponse response = OrderResponse.from(paymentService.confirm(user, request));
        return ResponseEntity.ok(ApiResponse.success("결제가 완료되었습니다.", response));
    }
}
