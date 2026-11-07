package com.mentor.paymentservice.payment;

import com.mentor.shared.api.ControllerHandler;
import com.mentor.shared.entity.HttpBodyResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    /** 201 for a new payment, 200 when the order already has a live pending payment. */
    @PostMapping
    public ResponseEntity<HttpBodyResponse<PaymentResponse>> create(
            @Valid @RequestBody CreatePaymentRequest request, @AuthenticationPrincipal Jwt jwt) {
        PaymentService.CreateResult result = service.create(userId(jwt), request.orderId());;
        return ControllerHandler.responseCreated(result.payment());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HttpBodyResponse<PaymentResponse>> get(
            @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ControllerHandler.responseSucceed(service.get(userId(jwt), id));
    }

    /** The client polls this every few seconds and stops as soon as the status is final. */
    @PostMapping("/{id}/check")
    public ResponseEntity<HttpBodyResponse<PaymentResponse>> check(
            @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ControllerHandler.responseSucceed(service.check(userId(jwt), id));
    }

    // The user id is the subject of the VERIFIED token. It is never read from a header, body or query.
    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}