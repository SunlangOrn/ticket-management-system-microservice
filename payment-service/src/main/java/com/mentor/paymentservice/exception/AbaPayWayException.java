package com.mentor.paymentservice.exception;

import com.mentor.shared.exception.HttpException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class AbaPayWayException extends HttpException {

    public AbaPayWayException(HttpStatus status, String message) {
        super(status, message);
    }

    public static AbaPayWayException notFound(UUID paymentId) {
        return new AbaPayWayException(
                HttpStatus.NOT_FOUND,
                "Payment not found with ID: " + paymentId
        );
    }

    public static AbaPayWayException unavailable() {
        return new AbaPayWayException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "The payment provider is temporarily unavailable. Please try again later."
        );
    }
}
