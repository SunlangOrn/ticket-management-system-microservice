package com.mentor.paymentservice.payment;

import com.mentor.paymentservice.exception.PaymentException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, updatable = false, length = 30)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.ABA_PAYWAY_KHQR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false, length = 3)
    private String currency;

    // The id we give ABA (tran_id)
    @Column(name = "tran_id", nullable = false, updatable = false, length = 20)
    private String tranId;

    @Column(name = "qr_string", columnDefinition = "text")
    private String qrString;

    @Column(columnDefinition = "text")
    private String deeplink;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void attachQr(String qr, String link) {
        if (status != PaymentStatus.PENDING) {
            throw PaymentException.cannotAttachQr("QR code can only be attached to a pending payment");
        }
        this.qrString = qr;
        this.deeplink = link;
    }

    public boolean isOverdue(Instant now, Duration grace) {
        return now.isAfter(expiresAt.plus(grace));
    }

    public boolean canCheck(Instant now, Duration cooldown) {
        return lastCheckedAt == null || lastCheckedAt.plus(cooldown).isBefore(now);
    }

    public void markChecked(Instant now) {
        this.lastCheckedAt = now;
    }

    public boolean markPaid(Instant now, Duration grace) {
        switch (status) {
            case PENDING -> {
                this.paidAt = now;
                this.status = isOverdue(now, grace) ? PaymentStatus.REFUND_REQUIRED : PaymentStatus.COMPLETED;
                return true;
            }
            case COMPLETED, REFUND_REQUIRED -> {
                return false;
            }
            default -> {
                this.paidAt = now;
                this.status = PaymentStatus.REFUND_REQUIRED;
                return true;
            }
        }
    }

    public boolean markFailed() {
        if (status == PaymentStatus.PENDING) {
            this.status = PaymentStatus.FAILED;
            return true;
        }
        if (status == PaymentStatus.FAILED || status == PaymentStatus.EXPIRED) {
            return false;
        }
        throw PaymentException.cannotFail("Payment failed");
    }

    public void expire() {
        if (status != PaymentStatus.PENDING) {
            throw PaymentException.cannotExpire("Payment failed");
        }
        this.status = PaymentStatus.EXPIRED;
    }
}
