package com.mentor.eventservice.event;


import java.util.UUID;

import com.mentor.shared.entity.PaginationQuery;
import com.mentor.shared.entity.Paging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mentor.eventservice.event.EventException.notFound;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository repository;
    private final EventMapper mapper;

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        Event saved = repository.save(mapper.toEntity(request));
        log.info("Event created id={} title={}", saved.getId(), saved.getTitle());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EventResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public Paging<EventResponse> listPublished(String city, PaginationQuery query) {
        Pageable pageable = PageRequest.of(query.zeroBasedPage(), query.getSize(), Sort.by("startsAt"));

        Page<Event> page = (city == null || city.isBlank())
                ? repository.findByStatus(EventStatus.PUBLISHED, pageable)
                : repository.findByStatusAndCityIgnoreCase(EventStatus.PUBLISHED, city.trim(), pageable);

        return Paging.of(
                page.map(mapper::toResponse).getContent(),
                query.getSize(),
                query.getPage(),
                page.getTotalElements(),
                page.getTotalPages() // Don't forget totalPages!
        );
    }

    @Transactional
    public EventResponse publish(Long id) {
        Event event = find(id);
        event.publish(); // dirty checking saves it on commit
        log.info("Event published id={}", id);
        return mapper.toResponse(event);
    }

    private Event find(Long id) {
        return repository.findById(id)
                .orElseThrow(EventException::notFound);
    }
}
