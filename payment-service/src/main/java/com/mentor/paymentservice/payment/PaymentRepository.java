package com.mentor.paymentservice.payment;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /** Looks up by owner too, so one user can never read another user's payment. */
    Optional<Payment> findByIdAndUserId(UUID id, UUID userId);

    Optional<Payment> findByTranId(String tranId);

    /** With PaymentStatus.LIVE the unique index guarantees at most one row. */
    Optional<Payment> findByOrderIdAndStatusIn(UUID orderId, Collection<PaymentStatus> statuses);

    /** SELECT ... FOR UPDATE: a poll and a callback can never settle one payment at the same time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select p.id from Payment p
             where p.status = :status and p.expiresAt < :cutoff
             order by p.expiresAt
            """)
    List<UUID> findIdsByStatusAndExpiresAtBefore(
            @Param("status") PaymentStatus status,
            @Param("cutoff") Instant cutoff,
            Pageable pageable);
}