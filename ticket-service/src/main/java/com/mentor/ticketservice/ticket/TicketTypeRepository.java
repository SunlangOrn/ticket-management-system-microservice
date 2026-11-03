package com.mentor.ticketservice.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {

    List<TicketType> findByEventIdOrderByPriceAsc(UUID eventId);

    @Modifying
    @Query(value = """
            UPDATE ticket_types
               SET reserved = reserved + :qty
             WHERE id = :id
               AND quota - reserved - sold >= :qty
            """, nativeQuery = true)
    int tryReserve(@Param("id") UUID id, @Param("qty") int qty);

    @Modifying
    @Query(value = """
            UPDATE ticket_types
               SET reserved = reserved - :qty
             WHERE id = :id
               AND reserved >= :qty
            """, nativeQuery = true)
    int giveBack(@Param("id") UUID id, @Param("qty") int qty);

    @Modifying
    @Query(value = """
            UPDATE ticket_types
               SET reserved = reserved - :qty,
                   sold     = sold + :qty
             WHERE id = :id
               AND reserved >= :qty
            """, nativeQuery = true)
    int markSold(@Param("id") UUID id, @Param("qty") int qty);
}
