package com.mentor.eventservice.event;

import com.mentor.shared.api.ControllerHandler;
import com.mentor.shared.entity.HttpBodyPagingResponse;
import com.mentor.shared.entity.HttpBodyResponse;
import com.mentor.shared.entity.PaginationQuery;
import com.mentor.shared.entity.Paging;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.mentor.shared.api.ControllerHandler.responseCreated;
import static com.mentor.shared.api.ControllerHandler.responseSucceed;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@NullMarked
public class EventController {

    private final EventService service;

    @PostMapping
    public ResponseEntity<HttpBodyResponse<EventResponse>> create(
            @Valid @RequestBody CreateEventRequest request) {
        return responseCreated(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HttpBodyResponse<EventResponse>> get(@PathVariable UUID id) {
        return responseSucceed(service.get(id));
    }

    @GetMapping
    public ResponseEntity<HttpBodyResponse<List<EventResponse>>> list(
            @RequestParam(required = false) String city,
            @Valid PaginationQuery query) {

        Paging<EventResponse> result = service.listPublished(city, query);

        HttpBodyPagingResponse pagingResponse = HttpBodyPagingResponse.builder()
                .page(result.getPage())
                .size(result.getSize())
                .total(result.getTotal())
                .totalPages(result.getTotalPages())
                .build();
        return ControllerHandler.responsePaged(result.getItems(), pagingResponse);
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<HttpBodyResponse<EventResponse>> publish(@PathVariable UUID id) {
        return responseSucceed(service.publish(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<HttpBodyResponse<EventResponse>> cancel(@PathVariable UUID id) {
        return responseSucceed(service.cancel(id));
    }
}
