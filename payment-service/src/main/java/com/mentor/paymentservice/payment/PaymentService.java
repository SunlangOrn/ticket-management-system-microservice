package com.mentor.paymentservice.payment;

import com.mentor.paymentservice.aba.AbaPayWayGateway;
import com.mentor.paymentservice.aba.AbaPayWayGateway.QrCommand;
import com.mentor.paymentservice.aba.AbaPayWayGateway.QrResult;
import com.mentor.paymentservice.client.OrderServiceClient;
import com.mentor.paymentservice.client.OrderServiceClient.OrderInfo;
import com.mentor.paymentservice.common.BusinessRuleException;
import com.mentor.tms.share.exception.HttpException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Creating and reading payments. Asking ABA about the result lives in PaymentSettlementService.
 *
 * Rule of this class: a call to another system (order-service, ABA) NEVER runs inside a database
 * transaction. Each database step is its own short transaction, run through {@code tx}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String ORDER_WAITING_FOR_PAYMENT = "PENDING_PAYMENT";
    private static final int MAX_TRAN_ID_ATTEMPTS = 3;

    // The order of these fields is the order of the constructor (PaymentServiceTest relies on it)
    private final PaymentRepository payments;
    private final OrderServiceClient orders;
    private final AbaPayWayGateway aba;
    private final PaymentSettlementService settlement;
    private final TranIdGenerator tranIds;
    private final PaymentMapper mapper;
    private final PaymentProperties properties;
    private final TransactionOperations tx;

    /** {@code created} is false when the order already had a live pending payment (a repeated POST). */
    public record CreateResult(PaymentResponse payment, boolean created) { }

    // ---------------------------------------------------------------- create

    public CreateResult create(UUID userId, UUID orderId) {
        OrderInfo order = loadPayableOrder(orderId);               // remote call, no transaction
        Instant expiresAt = deadlineFor(order);

        Optional<PaymentResponse> existing = reuseLivePayment(orderId);
        if (existing.isPresent()) {
            return new CreateResult(existing.get(), false);        // same payment, no second QR
        }

        Payment payment = insertPending(userId, order, expiresAt); // transaction 1: the row exists first
        QrResult qr = requestQr(payment, order);                   // remote call, no transaction
        PaymentResponse response = storeQr(payment.getId(), qr);   // transaction 2

        log.info("Payment created id={} order={} amount={}", payment.getId(), orderId, payment.getAmount());
        return new CreateResult(response.withQrImage(qr.qrImage()), true);
    }

    /** The amount and the deadline come from order-service, which also checks the owner. */
    private OrderInfo loadPayableOrder(UUID orderId) {
        OrderInfo order = orders.getOrder(orderId);
        if (!ORDER_WAITING_FOR_PAYMENT.equals(order.status())) {
            throw new BusinessRuleException("The order is not waiting for payment (status: " + order.status() + ")");
        }
        if (order.totalAmount().signum() <= 0) {
            throw new BusinessRuleException("There is nothing to pay for this order");
        }
        return order;
    }

    /** The payment must end before the reservation does, with a safety margin. */
    private Instant deadlineFor(OrderInfo order) {
        Instant now = Instant.now();
        Instant byWindow = now.plus(properties.window());
        Instant byReservation = order.expiresAt().minus(properties.safetyMargin());
        Instant expiresAt = byWindow.isBefore(byReservation) ? byWindow : byReservation;

        if (Duration.between(now, expiresAt).compareTo(properties.minLifetime()) < 0) {
            throw new BusinessRuleException("The order is about to expire, please place a new order");
        }
        return expiresAt;
    }

    /**
     * A repeated POST must not create a second payment. An overdue one is closed first, but only
     * after asking ABA, because the buyer may have paid at the last moment.
     */
    private Optional<PaymentResponse> reuseLivePayment(UUID orderId) {
        Optional<Payment> live = payments.findByOrderIdAndStatusIn(orderId, PaymentStatus.LIVE);
        if (live.isEmpty()) {
            return Optional.empty();
        }
        UUID id = live.get().getId();

        boolean neverReceivedAQr = live.get().getStatus() == PaymentStatus.PENDING && live.get().getQrString() == null;
        if (neverReceivedAQr) {
            failQuietly(id);
            return Optional.empty();
        }

        settlement.settle(id, Duration.ZERO);
        Payment current = payments.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
        return switch (current.getStatus()) {
            case COMPLETED -> throw new HttpException(HttpStatus.CONFLICT, "This order is already paid");
            case PENDING -> Optional.of(mapper.toResponse(current));
            default -> Optional.empty(); // expired, failed or refund required: a new payment is allowed
        };
    }

    /** The row exists BEFORE we call ABA, so a crash after ABA answers still leaves us the tran id. */
    private Payment insertPending(UUID userId, OrderInfo order, Instant expiresAt) {
        for (int attempt = 1; attempt <= MAX_TRAN_ID_ATTEMPTS; attempt++) {
            try {
                return tx.execute(ignored -> payments.saveAndFlush(Payment.builder()
                        .orderId(order.id())
                        .userId(userId)
                        .amount(order.totalAmount())
                        .currency(properties.currency())
                        .tranId(tranIds.next())
                        .expiresAt(expiresAt)
                        .build()));
            } catch (DataIntegrityViolationException e) {
                if (payments.findByOrderIdAndStatusIn(order.id(), PaymentStatus.LIVE).isPresent()) {
                    // Another request created the live payment at the same moment: the unique index decided
                    throw conflict("A payment for this order is already in progress, please retry");
                }
                // Otherwise the random tran id collided: try again with a new one
            }
        }
        throw conflict("The payment could not be created, please retry");
    }

    private QrResult requestQr(Payment payment, OrderInfo order) {
        QrCommand command = new QrCommand(payment.getTranId(), payment.getAmount(), payment.getCurrency(),
                order.ticketTypeName(), order.quantity(), order.unitPrice(), lifetimeMinutes(payment.getExpiresAt()));
        try {
            return aba.createQr(command);
        } catch (RuntimeException e) {
            failQuietly(payment.getId()); // the buyer never got a QR, so the buyer can simply retry
            throw e;
        }
    }

    private PaymentResponse storeQr(UUID paymentId, QrResult qr) {
        return tx.execute(ignored -> {
            Payment payment = payments.findByIdForUpdate(paymentId)
                    .orElseThrow(() -> new PaymentNotFoundException(paymentId));
            payment.attachQr(qr.qrString(), qr.deeplink());
            return mapper.toResponse(payment);
        });
    }

    private void failQuietly(UUID paymentId) {
        try {
            tx.executeWithoutResult(ignored -> payments.findByIdForUpdate(paymentId)
                    .filter(payment -> payment.getStatus() == PaymentStatus.PENDING)
                    .ifPresent(Payment::markFailed));
        } catch (RuntimeException e) {
            log.warn("Could not mark payment {} as failed", paymentId, e);
        }
    }

    /** How long ABA should keep the QR alive, rounded up to whole minutes (at least one). */
    private static long lifetimeMinutes(Instant expiresAt) {
        long seconds = Duration.between(Instant.now(), expiresAt).toSeconds();
        return Math.max(1, (seconds + 59) / 60);
    }

    private static HttpException conflict(String message) {
        return new HttpException(HttpStatus.CONFLICT, message);
    }

    // ---------------------------------------------------------------- read

    @Transactional(readOnly = true)
    public PaymentResponse get(UUID userId, UUID paymentId) {
        // Someone else's payment is reported as "not found", never as "forbidden"
        return payments.findByIdAndUserId(paymentId, userId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    /** The buyer asks "did my payment arrive?". Safe to call as often as the client likes. */
    public PaymentResponse check(UUID userId, UUID paymentId) {
        payments.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        return settlement.refresh(paymentId, false);
    }
}