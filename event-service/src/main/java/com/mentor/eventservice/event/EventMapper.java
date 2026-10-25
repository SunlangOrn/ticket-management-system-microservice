package com.mentor.eventservice.event;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EventMapper {

    Event toEntity(CreateEventRequest request);

    EventResponse toResponse(Event event);
}
