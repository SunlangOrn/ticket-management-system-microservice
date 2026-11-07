package com.mentor.paymentservice.payment;

import java.util.List;

public enum PaymentStatus {
    PENDING, COMPLETED, FAILED, EXPIRED, REFUND_REQUIRED;

    /**
     * "Live" payments: an order can have at most one of them. This list must match the partial
     * unique index uq_payments_order_live in the database.
     */
    public static final List<PaymentStatus> LIVE = List.of(PENDING, COMPLETED);
}