package com.mentor.ticketservice.ticket;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketTypeResponse(
        UUID id,
        UUID eventId,
        String name,
        BigDecimal price,
        int quota,
        int reserved,
        int sold,
        int available) { }
