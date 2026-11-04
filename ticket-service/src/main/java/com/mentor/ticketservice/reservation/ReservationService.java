package com.mentor.ticketservice.reservation;

import java.util.UUID;

public interface ReservationService {

    ReservationResponse reserve(UUID userId, CreateReservationRequest request);

    ReservationResponse get(UUID userId, UUID reservationId);

    ReservationResponse cancel(UUID userId, UUID reservationId);

    ReservationResponse confirm(UUID reservationId);


}
