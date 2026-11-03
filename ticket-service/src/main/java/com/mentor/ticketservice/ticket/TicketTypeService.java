package com.mentor.ticketservice.ticket;

import com.mentor.ticketservice.exception.TicketException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository repository;
    private final TicketTypeMapper mapper;

    @Transactional(readOnly = true)
    public TicketTypeResponse get(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> TicketException.notFound(id));
    }

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> listByEvent(UUID eventId) {
        return repository.findByEventIdOrderByPriceAsc(eventId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public TicketTypeResponse create(CreateTicketTypeRequest request) {
        try {
            // saveAndFlush so a duplicate name fails HERE, not later at commit
            TicketType saved = repository.saveAndFlush(mapper.toEntity(request));
            log.info("Ticket type created id={} event={} quota={}",
                    saved.getId(), saved.getEventId(), saved.getQuota());
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw TicketException.alreadyExits();
        }
    }
}
