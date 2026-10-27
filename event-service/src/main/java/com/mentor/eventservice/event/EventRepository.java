package com.mentor.eventservice.event;

import java.util.UUID;


import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@NullMarked
public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    Page<Event> findByStatusAndCityIgnoreCase(EventStatus status, String city, Pageable pageable);
}
