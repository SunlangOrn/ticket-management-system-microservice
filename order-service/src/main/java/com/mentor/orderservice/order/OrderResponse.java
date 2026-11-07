package com.mentor.orderservice.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID eventId,
        UUID ticketTypeId,
        String ticketTypeName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal totalAmount,
        UUID reservationId,
        OrderStatus status,
        Instant expiresAt,
        Instant createdAt) { }