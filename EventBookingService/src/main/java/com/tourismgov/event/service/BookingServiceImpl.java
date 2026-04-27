package com.tourismgov.event.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.event.client.NotificationClient;
import com.tourismgov.event.client.TouristClient;
import com.tourismgov.event.client.UserClient;
import com.tourismgov.event.dto.AuditLogRequest;
import com.tourismgov.event.dto.BookingRequest;
import com.tourismgov.event.dto.BookingResponse;
import com.tourismgov.event.dto.TouristDTO;
import com.tourismgov.event.dto.UpdateBookingStatusRequest;
import com.tourismgov.event.entity.Booking;
import com.tourismgov.event.entity.Event;
import com.tourismgov.event.enums.BookingStatus;
import com.tourismgov.event.exceptions.ErrorMessages;
import com.tourismgov.event.exceptions.ResourceNotFoundException;
import com.tourismgov.event.repository.BookingRepository;
import com.tourismgov.event.repository.EventRepository;
import com.tourismgov.event.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final String RESOURCE_BOOKING = "BookingService";
    private static final String ENTITY_BOOKING = "Booking";
    private static final String ENTITY_EVENT = "Event";
    private static final String ENTITY_TOURIST = "Tourist";
    
    private static final String ACTION_BOOKING_CREATE = "BOOKING_CREATE";
    private static final String ACTION_BOOKING_STATUS_UPDATE = "BOOKING_STATUS_UPDATE";
    
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    
    private final TouristClient touristClient;
    private final UserClient userClient;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public BookingResponse createBooking(Long eventId, BookingRequest request) {
        log.info("Creating booking for Event ID: {} for Tourist ID: {}", eventId, request.getTouristId());
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Fetch Local Event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_EVENT, eventId));
        
        // 2. Fetch Remote Tourist Profile
        TouristDTO tourist;
        try {
            tourist = touristClient.getTouristById(request.getTouristId());
        } catch (Exception e) {
            log.error("Failed to fetch tourist profile", e);
            throw new ResourceNotFoundException(ENTITY_TOURIST, request.getTouristId());
        }

        // 3. Duplicate Booking Check
        if (bookingRepository.existsByEvent_EventIdAndTouristId(eventId, request.getTouristId())) {
            log.warn("Duplicate booking attempt by Tourist ID: {} for Event ID: {}", request.getTouristId(), eventId);
            logAuditSafe(currentUserId, ACTION_BOOKING_CREATE, RESOURCE_BOOKING, STATUS_FAILED);
            throw new IllegalArgumentException(ErrorMessages.DUPLICATE_BOOKING);
        }

        // ================================================================
        // 4. Tourist Status Check (UPDATED LOGIC)
        // Block the booking if the tourist is anything OTHER than "ACTIVE"
        // ================================================================
        if (!"ACTIVE".equalsIgnoreCase(tourist.getStatus())) {
            log.warn("Booking denied: Tourist ID {} is not active. Current status: {}", tourist.getTouristId(), tourist.getStatus());
            logAuditSafe(currentUserId, ACTION_BOOKING_CREATE, RESOURCE_BOOKING, STATUS_FAILED);
            
            // Throwing a clear message prompting them to verify their profile
            throw new IllegalStateException("Please verify your profile first. Only active tourists can book events.");
        }

        // 5. Save Booking
        Booking booking = new Booking();
        booking.setEvent(event); 
        booking.setTouristId(tourist.getTouristId()); 
        booking.setNumberOfTickets(request.getNumberOfTickets() != null ? request.getNumberOfTickets() : 1);
        booking.setDate(LocalDateTime.now());
        
        if (request.getStatus() != null) {
            booking.setStatus(request.getStatus());
        } else {
            booking.setStatus(BookingStatus.CONFIRMED); 
        }

        Booking savedBooking = bookingRepository.save(booking);
        
        // 6. External Triggers (Audit Log & Notification)
        logAuditSafe(currentUserId, ACTION_BOOKING_CREATE, RESOURCE_BOOKING, STATUS_SUCCESS);
        
        // Perfectly formatted notification message using the event details
        String message = "Your trip to " + event.getTitle() + " has been confirmed for " + event.getDate() + ". Have a safe journey!";
        sendSystemAlertSafe(tourist.getUserId(), savedBooking.getBookingId(), "Booking Confirmed!", message, "BOOKING");

        return mapToResponse(savedBooking);
    }
    
    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, UpdateBookingStatusRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_BOOKING, bookingId));

        BookingStatus oldStatus = booking.getStatus();

        if (request.getStatus() != null) {
            booking.setStatus(request.getStatus());
        }

        Booking updatedBooking = bookingRepository.save(booking);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        logAuditSafe(currentUserId, ACTION_BOOKING_STATUS_UPDATE, RESOURCE_BOOKING, STATUS_SUCCESS);
        
        if (!oldStatus.equals(updatedBooking.getStatus())) {
            String message = String.format("The status of your booking for %s has been updated to: %s.", 
                    booking.getEvent().getTitle(), updatedBooking.getStatus().name());

            try {
                TouristDTO tourist = touristClient.getTouristById(booking.getTouristId());
                sendSystemAlertSafe(tourist.getUserId(), booking.getBookingId(), "Booking Status Update", message, "BOOKING");
            } catch (Exception e) {
                log.warn("Could not send notification. Tourist mapping failed.");
            }
        }
        
        return mapToResponse(updatedBooking);
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_BOOKING, bookingId)); 
    }

    @Override
    public List<BookingResponse> getBookingsByEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException(ENTITY_EVENT, eventId);
        }
        return bookingRepository.findByEvent_EventId(eventId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByTourist(Long touristId) {
        return bookingRepository.findByTouristId(touristId).stream().map(this::mapToResponse).toList(); 
    }

    @Override
    public Page<BookingResponse> getAllBookingsPaged(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (status != null && !status.isBlank()) {
            try {
                BookingStatus statusEnum = BookingStatus.valueOf(status.toUpperCase());
                return bookingRepository.findByStatus(statusEnum, pageable).map(this::mapToResponse);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(ErrorMessages.INVALID_STATUS);
            }
        }
        return bookingRepository.findAll(pageable).map(this::mapToResponse); 
    }

    @Override
    public Page<BookingResponse> getBookingsByEventPaged(Long eventId, int page, int size) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException(ENTITY_EVENT, eventId);
        }
        Pageable pageable = PageRequest.of(page, size);
        return bookingRepository.findByEvent_EventId(eventId, pageable).map(this::mapToResponse);
    }

    // --- Private Fault-Tolerant External Methods ---

    private void logAuditSafe(Long userId, String action, String resource, String status) {
        try {
            AuditLogRequest auditRequest = new AuditLogRequest();
            auditRequest.setUserId(userId);
            auditRequest.setAction(action);
            auditRequest.setResource(resource);
            auditRequest.setStatus(status);
            
            userClient.logAction(auditRequest);
        } catch (Exception e) {
            log.error("Failed to push audit log to USER-SERVICE", e);
        }
    }

    private void sendSystemAlertSafe(Long userId, Long entityId, String subject, String message, String category) {
        try {
            notificationClient.sendSystemAlert(userId, entityId, subject, message, category);
        } catch (Exception e) {
            log.error("Failed to push system alert", e);
        }
    }

    // --- Mapper ---

    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setTouristId(booking.getTouristId());
        
        if (booking.getEvent() != null) {
            response.setEventId(booking.getEvent().getEventId());
        }
        
        response.setDate(booking.getDate());
        response.setNumberOfTickets(booking.getNumberOfTickets());
        
        if(booking.getStatus() != null) {
            response.setStatus(booking.getStatus().name());
        }
        return response;
    }
}