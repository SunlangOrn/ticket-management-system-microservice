package com.mentor.ticketservice.reservation;

import com.mentor.ticketservice.exception.TicketException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ticket_type_id", nullable = false, updatable = false)
    private UUID ticketTypeId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public void confirm(Instant now) {
        requireHeld("confirmed");
        if (!expiresAt.isAfter(now)) {
            throw TicketException.reservationIsExpired();
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void release() {
        requireHeld("released");
        this.status = ReservationStatus.RELEASED;
    }

    public void expire() {
        requireHeld("expired");
        this.status = ReservationStatus.EXPIRED;
    }

    private void requireHeld(String action) {
        if (status != ReservationStatus.HELD) {
           throw TicketException.invalidStatus(action, status.name());
        }
    }
}
