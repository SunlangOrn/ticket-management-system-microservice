package com.mentor.eventservice.event;

import com.mentor.shared.api.ControllerHandler;
import com.mentor.shared.entity.HttpBodyPagingResponse;
import com.mentor.shared.entity.HttpBodyResponse;
import com.mentor.shared.entity.PaginationQuery;
import com.mentor.shared.entity.Paging;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mentor.shared.api.ControllerHandler.responseCreated;
import static com.mentor.shared.api.ControllerHandler.responseSucceed;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @PostMapping
    public ResponseEntity<HttpBodyResponse<EventResponse>> create(
            @Valid @RequestBody CreateEventRequest request) {
        return responseCreated(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HttpBodyResponse<EventResponse>> get(@PathVariable Long id) {
        return responseSucceed(service.get(id));
    }

    @GetMapping
    public ResponseEntity<HttpBodyResponse<List<EventResponse>>> list(
            @RequestParam(required = false) String city,
            @Valid PaginationQuery query) {

        Paging<EventResponse> result = service.listPublished(city, query);

        // 1. បង្កើត paging response ពីទិន្នន័យក្នុង Paging object
        HttpBodyPagingResponse pagingResponse = HttpBodyPagingResponse.builder()
                .page(result.getPage())
                .size(result.getSize())
                .totalPages(result.getTotal())
                .totalPages(result.getTotalPages())
                .build();

        // 2. ប្រើប្រាស់ responsePaged ឲ្យត្រូវនឹង ControllerHandler
        return ControllerHandler.responsePaged(result.getItems(), pagingResponse);
    }
    @PatchMapping("/{id}/publish")
    public ResponseEntity<HttpBodyResponse<EventResponse>> publish(@PathVariable Long id) {
        return responseSucceed(service.publish(id));
    }
}
