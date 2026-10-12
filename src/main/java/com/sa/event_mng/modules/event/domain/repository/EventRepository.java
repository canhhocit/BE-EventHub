package com.sa.event_mng.modules.event.domain.repository;

import com.sa.event_mng.modules.event.domain.model.Event;
import com.sa.event_mng.modules.event.domain.model.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    List<Event> findByStatusIn(Collection<EventStatus> statuses);

    Page<Event> findByStatusIn(Collection<EventStatus> statuses, Pageable pageable);

    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Event> findByNameContainingIgnoreCaseAndStatus(String name, EventStatus status, Pageable pageable);
    
    Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);
    
    List<Event> findByOrganizerId(Long organizerId);
}
