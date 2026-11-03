package com.mentor.ticketservice.ticket;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateTicketTypeRequest(
        @NotNull UUID eventId,
        @NotBlank @Size(max = 100) String name,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @Min(1) @Max(1_000_000) int quota) { }
