package com.mentor.ticketservice.ticket;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_types")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quota;

    @Column(nullable = false, updatable = false)
    private int reserved;

    @Column(nullable = false, updatable = false)
    private int sold;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    private TicketType(UUID eventId, String name, BigDecimal price, int quota) {
        this.eventId = eventId;
        this.name = name;
        this.price = price;
        this.quota = quota;
        this.createdAt = Instant.now();
    }

    public int getAvailable() {
        return quota - reserved - sold;
    }
}
