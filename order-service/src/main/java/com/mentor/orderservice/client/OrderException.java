package com.mentor.orderservice.client;

import com.mentor.shared.exception.HttpException;
import org.springframework.http.HttpStatus;

public class OrderException extends HttpException {

    public static OrderException cannotCancel(String currentStatus) {
        return new OrderException(
                HttpStatus.CONFLICT,
                "Only orders waiting for payment can be cancelled (current status: "
                        + currentStatus + ")"
        );
    }

    public OrderException(HttpStatus status, String message) {
        super(status,message);
    }
}
