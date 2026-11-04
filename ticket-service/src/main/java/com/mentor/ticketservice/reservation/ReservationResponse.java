package com.mentor.ticketservice.reservation;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID ticketTypeId,
        UUID userId,
        int quantity,
        ReservationStatus status,
        Instant expiresAt,
        Instant createdAt) { }
