package com.mentor.orderservice.order;

import com.mentor.orderservice.client.TicketServiceClient;
import com.mentor.orderservice.exception.OrderException;
import com.mentor.shared.exception.HttpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final TicketServiceClient ticketClient;


    @Override
    public OrderResponse create(UUID userId, CreateOrderRequest request) {
        TicketServiceClient.TicketTypeInfo ticketType = ticketClient.getTicketType(request.ticketTypeId());

        // No "is there enough?" pre-check: it would race. The atomic reserve decides.
        TicketServiceClient.ReservationInfo reservation = ticketClient.reserve(ticketType.id(), request.quantity());

        try {
            Order saved = repository.save(Order.builder()
                    .userId(userId)
                    .eventId(ticketType.eventId())
                    .ticketTypeId(ticketType.id())
                    .ticketTypeName(ticketType.name())
                    .unitPrice(ticketType.price())
                    .quantity(request.quantity())
                    .reservationId(reservation.id())
                    .expiresAt(reservation.expiresAt())
                    .build());
            log.info("Order created id={} reservation={} total={}",
                    saved.getId(), reservation.id(), saved.getTotalAmount());
            return mapper.toResponse(saved);
        } catch (RuntimeException e) {
            log.error("Saving the order failed after reserving {}: releasing the tickets", reservation.id(), e);
            releaseQuietly(reservation.id());
            throw e;
        }
    }

    @Override
    public OrderResponse get(UUID userId, UUID orderId) {
        return mapper.toResponse(findByOrderIdAndUserId(orderId, userId));
    }

    @Override
    public OrderResponse cancel(UUID userId, UUID orderId) {
        Order order = findByOrderIdAndUserId(userId, orderId);
        order.cancel();
        Order saved;
        try {
            saved = repository.save(order); // @Version rejects a stale copy
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new HttpException(HttpStatus.CONFLICT,
                    "The order was changed by another request, please retry");
        }
        releaseQuietly(saved.getReservationId());
        log.info("Order cancelled id={}", orderId);
        return mapper.toResponse(saved);
    }

    private Order findByOrderIdAndUserId(UUID userId, UUID orderId) {
        return repository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> OrderException.notFound(orderId));
    }

    private void releaseQuietly(UUID reservationId) {
        try {
            ticketClient.release(reservationId);
        } catch (RuntimeException e) {
            log.error("Could not release reservation {}; it will expire on its own", reservationId, e);
        }
    }
}
