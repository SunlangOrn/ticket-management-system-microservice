package com.mentor.orderservice.order;

import com.mentor.shared.api.ControllerHandler;
import com.mentor.shared.entity.HttpBodyResponse;
import jakarta.validation.Valid;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
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
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@NullMarked
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<HttpBodyResponse<OrderResponse>> create(
            @Valid @RequestBody CreateOrderRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ControllerHandler.responseCreated(service.create(userId(jwt), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HttpBodyResponse<OrderResponse>> get(
            @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ControllerHandler.responseSucceed(service.get(userId(jwt), id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<HttpBodyResponse<OrderResponse>> cancel(
            @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ControllerHandler.responseSucceed(service.cancel(userId(jwt), id));
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(Objects.requireNonNull(jwt.getSubject()));
    }
}