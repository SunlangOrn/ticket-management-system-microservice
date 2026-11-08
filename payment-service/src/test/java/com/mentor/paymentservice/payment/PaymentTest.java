package com.mentor.paymentservice.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mentor.paymentservice.exception.PaymentException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PaymentTest {

    private final Instant now = Instant.parse("2027-01-01T10:00:00Z");
    private final Duration grace = Duration.ofMinutes(1);

    private Payment pending(Instant expiresAt) {
        return Payment.builder()
                .orderId(UUID.randomUUID()).userId(UUID.randomUUID())
                .amount(new BigDecimal("40.00")).currency("USD")
                .tranId("TTESTTRAN0000001").expiresAt(expiresAt).build();
    }

    @Test
    void startsPendingWithTheKhqrMethod() {
        Payment payment = pending(now.plusSeconds(60));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.ABA_PAYWAY_KHQR);
    }

    @Test
    void paidBeforeExpiryCompletes() {
        Payment payment = pending(now.plusSeconds(60));
        assertThat(payment.markPaid(now, grace)).isTrue();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getPaidAt()).isEqualTo(now);
    }

    @Test
    void paidWithinTheGraceAfterExpiryStillCompletes() {
        Payment payment = pending(now.minusSeconds(30));
        payment.markPaid(now, grace);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void paidAfterTheGraceNeedsARefund() {
        Payment payment = pending(now.minusSeconds(120));
        payment.markPaid(now, grace);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUIRED);
    }

    @Test
    void repeatedPaidAnswerChangesNothing() {
        Payment payment = pending(now.plusSeconds(60));
        payment.markPaid(now, grace);
        assertThat(payment.markPaid(now.plusSeconds(5), grace)).isFalse();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getPaidAt()).isEqualTo(now);
    }

    @Test
    void paidAfterFailureOrExpiryNeedsARefund() {
        Payment failed = pending(now.plusSeconds(60));
        failed.markFailed();
        failed.markPaid(now, grace);
        assertThat(failed.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUIRED);

        Payment expired = pending(now.plusSeconds(60));
        expired.expire();
        expired.markPaid(now, grace);
        assertThat(expired.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUIRED);
    }

    @Test
    void aCompletedPaymentCannotFailOrExpire() {
        Payment payment = pending(now.plusSeconds(60));
        payment.markPaid(now, grace);
        assertThatThrownBy(payment::markFailed).isInstanceOf(PaymentException.class);
        assertThatThrownBy(payment::expire).isInstanceOf(PaymentException.class);
    }

    @Test
    void cooldownLimitsHowOftenWeAskAba() {
        Payment payment = pending(now.plusSeconds(60));
        Duration cooldown = Duration.ofSeconds(2);
        assertThat(payment.canCheck(now, cooldown)).isTrue();
        payment.markChecked(now);
        assertThat(payment.canCheck(now.plusSeconds(1), cooldown)).isFalse();
        assertThat(payment.canCheck(now.plusSeconds(3), cooldown)).isTrue();
    }
}
