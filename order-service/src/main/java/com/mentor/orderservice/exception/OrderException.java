package com.mentor.orderservice.exception;

import com.mentor.shared.exception.HttpException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class OrderException extends HttpException {

    public static OrderException cannotCancel(String currentStatus) {
        return new OrderException(
                HttpStatus.CONFLICT,
                "Only orders waiting for payment can be cancelled (current status: "
                        + currentStatus + ")"
        );
    }

    public static OrderException notFound(UUID orderId) {
        return new OrderException(
                HttpStatus.NOT_FOUND, "Order with id " + orderId + " was not found"
        );
    }

    public OrderException(HttpStatus status, String message) {
        super(status,message);
    }
}
