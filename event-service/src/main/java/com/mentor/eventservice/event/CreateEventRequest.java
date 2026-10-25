package com.mentor.eventservice.event;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateEventRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 150) String venueName,
        @NotBlank @Size(max = 100) String city,
        @NotNull @Future Instant startsAt,
        @NotNull Instant endsAt) { }