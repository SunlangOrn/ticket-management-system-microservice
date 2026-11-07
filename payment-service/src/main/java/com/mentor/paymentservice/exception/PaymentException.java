package com.mentor.paymentservice.exception;

import com.mentor.shared.exception.HttpException;
import org.springframework.http.HttpStatus;

public class PaymentException extends HttpException {

    public PaymentException(HttpStatus status, String message) {
        super(status, message);
    }

    public static PaymentException orderNotPayable(String status) {
        return new PaymentException(
                HttpStatus.CONFLICT, "The order is not waiting for payment (status: " + status + ")"
        );
    }

    public static PaymentException nothingToPay() {
        return new PaymentException(
                HttpStatus.CONFLICT, "There is nothing to pay for this order"
        );
    }

    public static PaymentException orderAboutToExpire() {
        return new PaymentException(
                HttpStatus.CONFLICT, "The order is about to expire. Please place a new order."
        );
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