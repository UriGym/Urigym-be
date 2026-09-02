package com.urigym.domain.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Calls Toss Payments' server-to-server confirm API. The secret key never reaches the
 * frontend — the widget on the client only ever sees the public client key.
 */
@Slf4j
@Component
public class TossPaymentClient {

    private final RestClient restClient = RestClient.create("https://api.tosspayments.com");
    private final String secretKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TossPaymentClient(@Value("${toss.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    /** @throws IllegalArgumentException with a message safe to show the user, if Toss rejects the payment. */
    public void confirm(String paymentKey, String orderId, int amount) {
        String credentials = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        try {
            restClient.post()
                    .uri("/v1/payments/confirm")
                    .header("Authorization", "Basic " + credentials)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.warn("Toss payment confirm rejected: {}", e.getResponseBodyAsString());
            throw new IllegalArgumentException(extractMessage(e));
        }
    }

    private String extractMessage(RestClientResponseException e) {
        try {
            Map<?, ?> body = objectMapper.readValue(e.getResponseBodyAsByteArray(), Map.class);
            Object message = body.get("message");
            if (message != null) {
                return "결제 승인에 실패했습니다: " + message;
            }
        } catch (Exception parseError) {
            // Fall through to the generic message below — the raw body is already logged.
        }
        return "결제 승인에 실패했습니다.";
    }
}
