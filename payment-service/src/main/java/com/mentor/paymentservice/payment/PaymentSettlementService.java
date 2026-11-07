package com.mentor.paymentservice.payment;

import com.mentor.paymentservice.aba.AbaPayWayGateway;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

import com.mentor.paymentservice.exception.AbaPayWayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Everything that asks ABA "did the money arrive?" and records the answer.
 *
 * Always three separate steps:
 *   1) short transaction : decide whether to ask, and remember that we asked
 *   2) NO transaction    : ask ABA
 *   3) short transaction : record the answer
 * A remote call must never hold a database connection or a row lock.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSettlementService {

    private static final Pattern TRAN_ID_FORMAT = Pattern.compile("[A-Za-z0-9]{1,20}");

    private final PaymentRepository payments;
    private final AbaPayWayGateway aba;
    private final PaymentMapper mapper;
    private final PaymentProperties properties;
    private final TransactionOperations tx;

    /** Outcome of step 1: either "no need to ask ABA" (with the answer to return) or "ask about this tran id". */
    private record CheckPlan(String tranIdToAsk, PaymentResponse answerWithoutAsking) {

        static CheckPlan skip(PaymentResponse response) {
            return new CheckPlan(null, response);
        }

        static CheckPlan ask(String tranId) {
            return new CheckPlan(tranId, null);
        }

        boolean needsAba() {
            return tranIdToAsk != null;
        }
    }

    // ---------------------------------------------------------------- entry points

    /**
     * Asks ABA (unless the payment is already decided, or was asked about a moment ago) and records
     * the answer. {@code force} ignores the cooldown. Throws 503 when ABA is unreachable: the
     * payment then simply stays PENDING.
     */
    public PaymentResponse refresh(UUID paymentId, boolean force) {
        CheckPlan plan = decideWhetherToAsk(paymentId, force);
        if (!plan.needsAba()) {
            return plan.answerWithoutAsking();
        }
        boolean paid = aba.isPaid(plan.tranIdToAsk());
        return recordAnswer(paymentId, paid);
    }

    /** Asks ABA one last time and only then, if it still says nothing, lets an overdue payment expire. */
    public void settle(UUID paymentId, Duration grace) {
        refresh(paymentId, true);
        expireIfStillPending(paymentId, grace);
    }

    /** Used by the scheduled job. */
    public void settleOverdue(UUID paymentId) {
        settle(paymentId, properties.safetyMargin());
    }

    /**
     * ABA's callback is only a hint. Its body is never read for a result: we only use the tran id
     * to find the payment, then we ask ABA ourselves. A forged callback therefore changes nothing.
     */
    public void onAbaCallback(String tranId) {
        if (tranId == null || !TRAN_ID_FORMAT.matcher(tranId).matches()) {
            return;
        }
        payments.findByTranId(tranId)
                .filter(payment -> payment.getStatus() == PaymentStatus.PENDING)
                .ifPresent(payment -> {
                    try {
                        refresh(payment.getId(), false);
                    } catch (RuntimeException e) {
                        log.warn("Could not verify the callback for payment {}", payment.getId(), e);
                    }
                });
    }

    // ---------------------------------------------------------------- the three steps

    private CheckPlan decideWhetherToAsk(UUID paymentId, boolean force) {
        return tx.execute(ignored -> {
            Payment payment = lock(paymentId);
            Instant now = Instant.now();
            boolean nothingToAsk = payment.getStatus() != PaymentStatus.PENDING || payment.getQrString() == null;
            boolean askedTooRecently = !force && !payment.canCheck(now, properties.checkCooldown());
            if (nothingToAsk || askedTooRecently) {
                return CheckPlan.skip(mapper.toResponse(payment));
            }
            payment.markChecked(now);
            return CheckPlan.ask(payment.getTranId());
        });
    }

    private PaymentResponse recordAnswer(UUID paymentId, boolean paid) {
        return tx.execute(ignored -> {
            Payment payment = lock(paymentId);
            if (paid && payment.markPaid(Instant.now(), properties.safetyMargin())) {
                logSettlement(payment);
            }
            return mapper.toResponse(payment);
        });
    }

    private void expireIfStillPending(UUID paymentId, Duration grace) {
        tx.executeWithoutResult(ignored -> payments.findByIdForUpdate(paymentId)
                .filter(payment -> payment.getStatus() == PaymentStatus.PENDING
                        && payment.isOverdue(Instant.now(), grace))
                .ifPresent(payment -> {
                    payment.expire();
                    log.info("Payment {} expired", paymentId);
                }));
    }

    // ---------------------------------------------------------------- helpers

    private Payment lock(UUID paymentId) {
        return payments.findByIdForUpdate(paymentId)
                .orElseThrow(() -> AbaPayWayException.notFound(paymentId));
    }

    private void logSettlement(Payment payment) {
        if (payment.getStatus() == PaymentStatus.REFUND_REQUIRED) {
            log.warn("Money arrived for payment {} but it can no longer be fulfilled: REFUND REQUIRED",
                    payment.getId());
        } else {
            log.info("Payment {} is now {}", payment.getId(), payment.getStatus());
        }
    }
}