package com.mentor.ticketservice.reservation;

import com.mentor.ticketservice.exception.TicketException;
import com.mentor.ticketservice.ticket.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService{

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final TicketTypeRepository ticketTypeRepository;
    private final ReservationProperties properties;


    @Override
    public ReservationResponse reserve(UUID userId, CreateReservationRequest request) {
        int updated = ticketTypeRepository.tryReserve(request.ticketTypeId(), request.quantity());

        if (updated == 0) {
            if (!ticketTypeRepository.existsById(request.ticketTypeId())) {
                throw TicketException.reservationNotFound(request.ticketTypeId());
            }
            throw TicketException.soldOut();
        }

        Reservation saved = reservationRepository.save(Reservation.builder()
                .ticketTypeId(request.ticketTypeId())
                .userId(userId)
                .quantity(request.quantity())
                .expiresAt(Instant.now().plus(properties.hold()))
                .build());

        log.info("Reserved {} x ticketType={} reservation={}", saved.getQuantity(), saved.getTicketTypeId(), saved.getId());

        return reservationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse get(UUID userId, UUID reservationId) {
        return reservationRepository.findByIdAndUserId(reservationId, userId)
                .map(reservationMapper::toResponse)
                .orElseThrow(() -> TicketException.reservationNotFound(reservationId));
    }

    @Transactional(readOnly = true)
    @Override
    public ReservationResponse cancel(UUID userId, UUID reservationId) {
        Reservation reservation = getOwnReservation(userId, reservationId);
        reservation.cancel();
        giveBack(reservation);
        log.info("Reservation released id={}", id);
        return reservationMapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    @Override
    public ReservationResponse confirm(UUID reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.confirm(Instant.now());
        giveBack(reservation);
        log.info("Reservation confirmed id={}", id);
        return reservationMapper.toResponse(reservation);
    }

    @Override
    public boolean expire(UUID id) {
        Reservation reservation = getReservation(id);

        if (reservation.getStatus().equals(ReservationStatus.HOLD)) {
            return false;
        }

        reservation.expire();
        log.info("Reservation expired id={}", id);

        return true;
    }

    private Reservation getReservation(UUID id) {
        return reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> TicketException.reservationNotFound(id));
    }

    private Reservation getOwnReservation(UUID userId, UUID id) {
        Reservation reservation = getReservation(id);
        if (!reservation.getUserId().equals(userId)) {
            throw TicketException.reservationNotFound(id);
        }
        return reservation;
    }

    private void giveBack(Reservation reservation) {
        requireOneRow(ticketTypeRepository.giveBack(reservation.getTicketTypeId(), reservation.getQuantity()),
                reservation);
    }

    private static void requireOneRow(int rows, Reservation reservation) {
        if (rows != 1) {
            throw new IllegalStateException("Inventory out of sync for reservation " + reservation.getId());
        }
    }
}
