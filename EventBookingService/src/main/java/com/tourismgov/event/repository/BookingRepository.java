package com.tourismgov.event.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tourismgov.event.entity.Booking;
import com.tourismgov.event.enums.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // 'Event' is an object, so we use Event_EventId. 'Tourist' is a flat ID, so we use TouristId.
    boolean existsByEvent_EventIdAndTouristId(Long eventId, Long touristId);
    
    List<Booking> findByEvent_EventId(Long eventId);
    Page<Booking> findByEvent_EventId(Long eventId, Pageable pageable);
    
    List<Booking> findByTouristId(Long touristId);
    Page<Booking> findByTouristId(Long touristId, Pageable pageable);

    List<Booking> findByStatus(BookingStatus status);
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
}