package com.mentor.ticketservice.ticket;

import com.mentor.shared.api.ControllerHandler;
import com.mentor.shared.entity.HttpBodyResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ticket-types")
@RequiredArgsConstructor
@NullMarked
public class TicketTypeController {

    private final TicketTypeService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<HttpBodyResponse<TicketTypeResponse>> create(
            @Valid @RequestBody CreateTicketTypeRequest request) {
        return ControllerHandler.responseCreated(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HttpBodyResponse<TicketTypeResponse>> get(@PathVariable UUID id) {
        return ControllerHandler.responseSucceed(service.get(id));
    }

    @GetMapping
    public ResponseEntity<HttpBodyResponse<List<TicketTypeResponse>>> listByEvent(
            @RequestParam UUID eventId) {
        return ControllerHandler.responseSucceed(service.listByEvent(eventId));
    }
}
