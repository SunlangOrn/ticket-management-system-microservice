package com.mentor.orderservice.order;

import java.util.UUID;

public interface OrderService {

    OrderResponse create(UUID userId, CreateOrderRequest request);

    OrderResponse get(UUID userId, UUID orderId);

    OrderResponse cancel(UUID userId, UUID orderId);
}
