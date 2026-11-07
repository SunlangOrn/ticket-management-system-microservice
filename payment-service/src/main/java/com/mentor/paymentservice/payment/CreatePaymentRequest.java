package com.mentor.paymentservice.payment;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Only the order id. The amount is NEVER accepted from the client. */
public record CreatePaymentRequest(@NotNull UUID orderId) { }