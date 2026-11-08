package com.mentor.paymentservice.payment;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpiryJob {

    private static final int BATCH_SIZE = 50;

    private final PaymentRepository repository;
    private final PaymentSettlementService settlement;
    private final PaymentProperties properties;

    @Scheduled(fixedDelayString = "${tms.payment.expiry-interval:PT30S}")
    public void settleOverduePayments() {
        Instant cutoff = Instant.now().minus(properties.safetyMargin());
        for (UUID id : repository.findIdsByStatusAndExpiresAtBefore(
                PaymentStatus.PENDING, cutoff, PageRequest.of(0, BATCH_SIZE))) {
            try {
                settlement.settleOverdue(id);
            } catch (RuntimeException e) {
                // ABA unreachable: the payment stays PENDING and the next run tries again
                log.warn("Could not settle payment {}", id, e);
            }
        }
    }
}