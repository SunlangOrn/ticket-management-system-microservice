package com.mentor.eventservice.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organizer_id")
    private UUID organizerId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "venue_name", nullable = false, length = 150)
    private String venueName;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    private Event(UUID organizerId, String title, String description, String venueName,
                  String city, Instant startsAt, Instant endsAt) {
        if (!endsAt.isAfter(startsAt)) {
            throw EventException.badRequest("endsAt must be after startsAt");
        }
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.venueName = venueName;
        this.city = city;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = EventStatus.DRAFT;
        this.createdAt = Instant.now();
    }

    public void publish() {
        if (status != EventStatus.DRAFT) {
            throw EventException.unprocessableEntity(
                    "Only DRAFT events can be published (current status: " + status + ")");
        }
        this.status = EventStatus.PUBLISHED;
    }

    public void cancel() {
        if (status != EventStatus.DRAFT && status != EventStatus.PUBLISHED) {
            throw EventException.unprocessableEntity(
                    "Only DRAFT or PUBLISHED events can be cancelled (current status: " + status + ")");
        }
        this.status = EventStatus.CANCELLED;
    }
}