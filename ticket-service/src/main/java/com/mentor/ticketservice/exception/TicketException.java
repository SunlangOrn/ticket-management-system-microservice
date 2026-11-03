package com.mentor.ticketservice.exception;

import com.mentor.shared.exception.ErrorConstantException;
import com.mentor.shared.exception.HttpException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class TicketException extends HttpException {

    public static TicketException notFound(UUID id) {
        return new TicketException(HttpStatus.NOT_FOUND, "Ticket type with id " + id + " was not found");
    }

    public static TicketException alreadyExits() {
        return new TicketException(HttpStatus.CONFLICT, "Ticket has already been exited");
    }

    public TicketException(HttpStatus status , String message) {
        super(status, message);
    }
}
