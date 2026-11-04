package com.mentor.ticketservice.reservation;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReservationMapper {

    ReservationResponse toResponse(Reservation reservation);
}
