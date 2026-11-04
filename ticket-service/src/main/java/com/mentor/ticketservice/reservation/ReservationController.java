package com.mentor.ticketservice.reservation;

import com.mentor.shared.api.ControllerHandler;
import com.mentor.shared.entity.HttpBodyResponse;
import jakarta.validation.Valid;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@NullMarked
public class ReservationController {

    private final ReservationService service;

    @PostMapping
    public ResponseEntity<HttpBodyResponse<ReservationResponse>> create(
            @Valid @RequestBody CreateReservationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ControllerHandler.responseCreated(service.reserve(userId(jwt), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HttpBodyResponse<ReservationResponse>> get(
            @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ControllerHandler.responseSucceed(service.get(userId(jwt), id));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<HttpBodyResponse<ReservationResponse>> release(
            @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ControllerHandler.responseSucceed(service.cancel(userId(jwt), id));
    }

    // Temporary: only an ADMIN may confirm by hand. Later it is driven by the OrderPaid event.
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpBodyResponse<ReservationResponse>> confirm(@PathVariable UUID id) {
        return ControllerHandler.responseSucceed(service.confirm(id));
    }

    // The user id is the subject of the VERIFIED token. It is never read from a header, body or query.
    private static UUID userId(Jwt jwt) {
        return UUID.fromString(Objects.requireNonNull(jwt.getSubject()));
    }
}