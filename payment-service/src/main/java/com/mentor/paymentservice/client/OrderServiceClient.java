package com.mentor.paymentservice.client;

import com.mentor.shared.entity.HttpBodyResponse;
import com.mentor.shared.exception.ErrorConstantException;
import com.mentor.shared.exception.HttpException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceClient {

    private static final Set<HttpStatus> PASS_THROUGH = EnumSet.of(
            HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND,
            HttpStatus.CONFLICT, HttpStatus.UNPROCESSABLE_ENTITY);

    private final RestClient orderRestClient;

    /** Only what payment-service needs from an order. Unknown fields are ignored on purpose. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderInfo(UUID id, String ticketTypeName, BigDecimal unitPrice, int quantity,
                            BigDecimal totalAmount, String status, Instant expiresAt) { }

    /** order-service answers 404 for an order that belongs to someone else, so ownership is checked there. */
    public OrderInfo getOrder(UUID orderId) {
        try {
            HttpBodyResponse<OrderInfo> body = orderRestClient.get()
                    .uri("/api/v1/orders/{id}", orderId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<HttpBodyResponse<OrderInfo>>() { });
            if (body == null || body.getData() == null) {
                log.error("order-service returned an empty body for order {}", orderId);
                throw unavailable();
            }
            return body.getData();
        } catch (RestClientResponseException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            if (status != null && PASS_THROUGH.contains(status)) {
                return fail(status, message(ex));
            }
            log.error("order-service answered {} for order {}", ex.getStatusCode(), orderId);
            throw unavailable();
        } catch (ResourceAccessException ex) {
            log.error("order-service unreachable or too slow for order {}", orderId, ex);
            throw unavailable();
        }
    }

    private static OrderInfo fail(HttpStatus status, String message) {
        throw new HttpException(status, message);
    }

    private static String message(RestClientResponseException ex) {
        try {
            HttpBodyResponse<?> body = ex.getResponseBodyAs(HttpBodyResponse.class);
            if (body != null && body.getMessage() != null) {
                return body.getMessage();
            }
        } catch (RuntimeException e) {
            // an unreadable error body must not hide the real status code
        }
        return "Request rejected by order service";
    }

    private static HttpException unavailable() {
        return new HttpException(HttpStatus.SERVICE_UNAVAILABLE, ErrorConstantException.ERROR,
                "Order service is temporarily unavailable, please try again");
    }
}
