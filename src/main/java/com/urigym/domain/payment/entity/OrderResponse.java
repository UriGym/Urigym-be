package com.urigym.domain.payment.entity;

import com.urigym.domain.payment.MembershipOrder;
import com.urigym.domain.payment.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String orderId;
    private String orderName;
    private Integer amount;
    private String customerName;
    private String customerEmail;
    private OrderStatus status;

    public static OrderResponse from(MembershipOrder order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderName(order.getOrderName())
                .amount(order.getAmount())
                .customerName(order.getUser().getFullName())
                .customerEmail(order.getUser().getEmail())
                .status(order.getStatus())
                .build();
    }
}
