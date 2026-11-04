package com.mentor.ticketservice.reservation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    Optional<Reservation> findByIdAndUserId(UUID id, UUID userId);


    /** SELECT ... FOR UPDATE: confirm, release and expire can never run on one row at once. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select r.id from Reservation r
             where r.status = :status and r.expiresAt < :now
             order by r.expiresAt
            """)
    List<UUID> findIdsByStatusAndExpiresAtBefore(
            @Param("status") ReservationStatus status,
            @Param("now") Instant now,
            Pageable pageable);
}
