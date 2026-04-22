package com.tourismgov.event.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourismgov.event.entity.Event;
import com.tourismgov.event.enums.EventStatus;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // CHANGED: 'site' and 'program' are now flat Long IDs, no underscore needed
    List<Event> findBySiteId(Long siteId);
    List<Event> findByProgramId(Long programId);

    List<Event> findBySiteIdAndProgramId(Long siteId, Long programId);

    // CHANGED: Uses EventStatus Enum. (Removed IgnoreCase as it's strongly typed now)
    List<Event> findByStatus(EventStatus status);
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    List<Event> findByDateAfter(LocalDateTime date);
}