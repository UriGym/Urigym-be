package com.urigym.domain.payment.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Exactly the three fields Toss appends to the success redirect URL. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequest {

    @NotBlank(message = "orderId가 필요합니다.")
    private String orderId;

    @NotBlank(message = "paymentKey가 필요합니다.")
    private String paymentKey;

    @NotNull(message = "amount가 필요합니다.")
    private Integer amount;
}
