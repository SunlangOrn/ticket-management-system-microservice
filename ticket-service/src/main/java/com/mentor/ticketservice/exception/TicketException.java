package com.mentor.ticketservice.exception;

import com.mentor.shared.exception.ErrorConstantException;
import com.mentor.shared.exception.HttpException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class TicketException extends HttpException {

    public static TicketException notFound(UUID id) {
        return new TicketException(HttpStatus.NOT_FOUND, "Ticket type with id " + id + " was not found");
    }

    public static TicketException  reservationNotFound(UUID id) {
        return new TicketException(HttpStatus.NOT_FOUND, "Reservation with id " + id + " was not found");
    }

    public static TicketException alreadyExits() {
        return new TicketException(HttpStatus.CONFLICT, "Ticket has already been exited");
    }

    public static TicketException reservationIsExpired () {
        return new TicketException(HttpStatus.GONE, "Ticket is expired");
    }

    public static TicketException soldOut() {
        return new TicketException(HttpStatus.CONFLICT, "Not enough tickets available");
    }

    public static TicketException invalidStatus(String action, String currentStatus
    ) {
        return new TicketException(
                HttpStatus.CONFLICT,
                "Reservation cannot be " + action + " (current status: " + currentStatus + ")"
        );
    }
    public TicketException(HttpStatus status , String message) {
        super(status, message);
    }
}
