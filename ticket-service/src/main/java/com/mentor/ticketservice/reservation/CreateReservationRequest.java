package com.mentor.ticketservice.reservation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReservationRequest(
        @NotNull UUID ticketTypeId,
        @Min(1) @Max(10) int quantity) { }
