package com.mentor.eventservice.event;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        UUID organizerId,
        String title, String
        description,
        String venueName,
        String city,
        Instant startsAt,
        Instant endsAt,
        EventStatus status,
        Instant createdAt) { }
