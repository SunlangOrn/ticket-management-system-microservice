package com.mentor.orderservice.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    /** Looks up by owner too, so one user can never read another user's order. */
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);
}
