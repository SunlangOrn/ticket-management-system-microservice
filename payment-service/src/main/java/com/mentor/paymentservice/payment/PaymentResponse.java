package com.mentor.paymentservice.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String qrString,
        String qrImage,
        String deeplink,
        Instant expiresAt,
        Instant paidAt,
        Instant createdAt) {

    /** The QR image is big and is only returned once, when the payment is created. */
    public PaymentResponse withQrImage(String image) {
        return new PaymentResponse(id, orderId, paymentMethod, amount, currency, status, qrString, image,
                deeplink, expiresAt, paidAt, createdAt);
    }
}