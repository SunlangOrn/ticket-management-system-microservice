package com.mentor.paymentservice.exception;

import com.mentor.shared.exception.HttpException;
import org.springframework.http.HttpStatus;

public class PaymentException extends HttpException {

    public PaymentException(HttpStatus status, String message) {
        super(status, message);
    }

    public static PaymentException cannotFail(String message) {
        return new PaymentException(HttpStatus.CONFLICT, message);
    }

    public static PaymentException cannotExpire(String message) {
        return new PaymentException(HttpStatus.CONFLICT, message
        );
    }

    public static PaymentException cannotAttachQr(String message) {
        return new PaymentException(HttpStatus.CONFLICT,message);
    }
}