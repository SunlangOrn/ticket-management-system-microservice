package com.mentor.orderservice.order;

import com.mentor.orderservice.exception.OrderException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.*;

@Setter
@Entity
@Table(name = "orders")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "ticket_type_id", nullable = false, updatable = false)
    private UUID ticketTypeId;

    @Column(name = "ticket_type_name", nullable = false, updatable = false, length = 100)
    private String ticketTypeName;

    @Column(name = "unit_price", nullable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Column(name = "total_amount", nullable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "reservation_id", nullable = false, updatable = false)
    private UUID reservationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public void cancel() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw OrderException.cannotCancel(status.name());
        }

        this.status = OrderStatus.CANCELLED;
    }
}