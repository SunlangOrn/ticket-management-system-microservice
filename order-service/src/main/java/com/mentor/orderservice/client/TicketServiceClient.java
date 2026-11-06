package com.mentor.orderservice.client;

import com.mentor.shared.entity.HttpBodyResponse;
import com.mentor.shared.exception.ErrorConstantException;
import com.mentor.shared.exception.HttpException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketServiceClient {

    // 4xx answers that describe the buyer's request, so they are safe to pass back
    private static final Set<HttpStatus> PASS_THROUGH = EnumSet.of(
            HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND,
            HttpStatus.CONFLICT, HttpStatus.UNPROCESSABLE_ENTITY);

    private final RestClient ticketRestClient;

    /** Only the fields order-service needs. Unknown fields are ignored on purpose. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TicketTypeInfo(UUID id, UUID eventId, String name, BigDecimal price) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReservationInfo(UUID id, Instant expiresAt) { }

    public record ReserveBody(UUID ticketTypeId, int quantity) { }

    public TicketTypeInfo getTicketType(UUID ticketTypeId) {
        return execute("get ticket type", () -> ticketRestClient.get()
                .uri("/api/v1/ticket-types/{id}", ticketTypeId)
                .retrieve()
                .body(new ParameterizedTypeReference<HttpBodyResponse<TicketTypeInfo>>() { }));
    }

    /** The buyer is identified by the relayed token, not by a parameter. */
    public ReservationInfo reserve(UUID ticketTypeId, int quantity) {
        return execute("reserve tickets", () -> ticketRestClient.post()
                .uri("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReserveBody(ticketTypeId, quantity))
                .retrieve()
                .body(new ParameterizedTypeReference<HttpBodyResponse<ReservationInfo>>() { }));
    }

    public void release(UUID reservationId) {
        execute("release reservation", () -> ticketRestClient.post()
                .uri("/api/v1/reservations/{id}/release", reservationId)
                .retrieve()
                .body(new ParameterizedTypeReference<HttpBodyResponse<ReservationInfo>>() { }));
    }

    private <T> T execute(String operation, Supplier<HttpBodyResponse<T>> call) {
        try {
            HttpBodyResponse<T> body = call.get();
            if (body == null || body.getData() == null) {
                log.error("ticket-service returned an empty body for '{}'", operation);
                throw unavailable();
            }
            return body.getData();
        } catch (RestClientResponseException ex) {
            throw translate(operation, ex);
        } catch (ResourceAccessException ex) {
            // Connection refused, connect timeout or read timeout
            log.error("ticket-service unreachable or too slow for '{}'", operation, ex);
            throw unavailable();
        }
    }

    private HttpException translate(String operation, RestClientResponseException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status != null && PASS_THROUGH.contains(status)) {
            log.debug("ticket-service rejected '{}' with {}", operation, status);
            return new HttpException(status, downstreamMessage(ex));
        }
        // 5xx, or a 401/403 that means our own configuration is wrong: not the buyer's fault.
        log.error("ticket-service answered {} for '{}'", ex.getStatusCode(), operation);
        return unavailable();
    }

    private static String downstreamMessage(RestClientResponseException ex) {
        try {
            HttpBodyResponse<?> body = ex.getResponseBodyAs(HttpBodyResponse.class);
            if (body != null && body.getMessage() != null) {
                return body.getMessage();
            }
        } catch (RuntimeException e) {
            // an unreadable error body must not hide the real status code
        }
        return "Request rejected by ticket service";
    }

    private static HttpException unavailable() {
        return new HttpException(HttpStatus.SERVICE_UNAVAILABLE, ErrorConstantException.ERROR,
                "Ticket service is temporarily unavailable, please try again");
    }
}