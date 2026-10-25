package com.mentor.eventservice.event;

import com.mentor.shared.exception.HttpException;
import org.springframework.http.HttpStatus;

public class EventException extends HttpException {

    public static EventException notFound() {
        return new EventException(HttpStatus.NOT_FOUND, "Event not found");
    }

    public static EventException badRequest(String message) {
        return new EventException(HttpStatus.BAD_REQUEST, message);
    }

    public EventException(HttpStatus httpStatus , String message) {
        super( httpStatus,message);
    }


}
