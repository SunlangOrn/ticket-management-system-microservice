package com.mentor.ticketservice.reservation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpiryJob {

    private final ReservationRepository repository;
    private final ReservationService service; // separate bean, so each expire() gets its own transaction

    @Scheduled(fixedDelayString = "${tms.reservation.expiry-interval:PT30S}")
    public void expireOverdueReservations() {
        List<UUID> ids = repository.findIdsByStatusAndExpiresAtBefore(
                ReservationStatus.HOLD, Instant.now(), PageRequest.of(0, 100));
        int expired = 0;
        for (UUID id : ids) {
            try {
                if (service.expire(id)) {
                    expired++;
                }
            } catch (RuntimeException e) {
                // One bad row must not stop the others
                log.warn("Could not expire reservation {}", id, e);
            }
        }
        if (expired > 0) {
            log.info("Expired {} reservation(s)", expired);
        }
    }
}