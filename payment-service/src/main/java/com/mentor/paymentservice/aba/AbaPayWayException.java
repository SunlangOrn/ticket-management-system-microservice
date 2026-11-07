package com.mentor.paymentservice.aba;

import com.mentor.shared.exception.ErrorConstantException;
import com.mentor.shared.exception.HttpException;
import org.springframework.http.HttpStatus;

public class AbaPayWayException extends HttpException {

    public AbaPayWayException(HttpStatus status, String message) {
        super(status, message);
    }

    public static AbaPayWayException unavailable() {
        return new AbaPayWayException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "The payment provider is temporarily unavailable. Please try again later."
        );
    }
}
