package com.tourismgov.event.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.event.client.NotificationClient;
import com.tourismgov.event.client.UserClient;
import com.tourismgov.event.dto.CreateEventRequest;
import com.tourismgov.event.dto.EventResponse;
import com.tourismgov.event.dto.UpdateEventStatusRequest;
import com.tourismgov.event.entity.Event;
import com.tourismgov.event.enums.EventStatus;
import com.tourismgov.event.exceptions.ResourceNotFoundException;
import com.tourismgov.event.repository.EventRepository;
import com.tourismgov.event.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final String RESOURCE_EVENT = "EventService";
    private static final String ENTITY_NAME = "Event";
    private static final String STATUS_SUCCESS = "SUCCESS";
    
    private static final String ACTION_EVENT_CREATE = "EVENT_CREATE";
    private static final String ACTION_EVENT_UPDATE = "EVENT_UPDATE";
    private static final String ACTION_EVENT_STATUS_UPDATE = "EVENT_STATUS_UPDATE";
    private static final String ACTION_EVENT_DELETE = "EVENT_DELETE";

    private final EventRepository eventRepository;
    private final UserClient userClient; 
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Creating event: {}", request.getTitle());

        Event event = new Event();
        event.setSiteId(request.getSiteId()); 
        event.setTitle(request.getTitle());
        event.setLocation(request.getLocation());
        event.setDate(request.getDate());
        
        // FIX: request.getStatus() is already an Enum, assign directly
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        } else {
            event.setStatus(EventStatus.SCHEDULED);
        }

        if (request.getProgramId() != null) {
            event.setProgramId(request.getProgramId());
        }

        Event saved = eventRepository.save(event);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        logAuditSafe(currentUserId, ACTION_EVENT_CREATE, RESOURCE_EVENT, STATUS_SUCCESS);
                
        String message = String.format("A new event '%s' has been scheduled at %s on %s.", 
                saved.getTitle(), saved.getLocation(), saved.getDate().toLocalDate());
                
        sendSystemAlertSafe(currentUserId, saved.getEventId(), "New Event Scheduled!", message, "EVENT");

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EventResponse updateEventStatus(Long eventId, UpdateEventStatusRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, eventId));
        
        EventStatus oldStatus = event.getStatus();
        
        // FIX: request.getStatus() is already an Enum
        if (request.getStatus() != null) {
            // Note: If your UpdateEventStatusRequest uses a String, you will need to change its type to EventStatus in that DTO, or use EventStatus.valueOf(request.getStatus()) here. Assuming you changed the DTO to Enum based on the error.
            event.setStatus(EventStatus.valueOf(request.getStatus().toString())); 
        }

        Event updatedEvent = eventRepository.save(event);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        logAuditSafe(currentUserId, ACTION_EVENT_STATUS_UPDATE, RESOURCE_EVENT, STATUS_SUCCESS);
        
        if (!oldStatus.equals(updatedEvent.getStatus())) {
            String message = String.format("Alert: Event '%s' status changed to %s.", 
                    event.getTitle(), updatedEvent.getStatus().name());
                    
            sendSystemAlertSafe(currentUserId, event.getEventId(), "Event Status Update", message, "ALERT");
        }
        
        return mapToResponse(updatedEvent);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long eventId, CreateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, eventId));

        event.setTitle(request.getTitle());
        event.setLocation(request.getLocation());
        event.setDate(request.getDate());

        if (request.getProgramId() != null) {
            event.setProgramId(request.getProgramId());
        }

        // FIX: request.getStatus() is already an Enum, assign directly
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }
        
        Event updatedEvent = eventRepository.save(event);
        logAuditSafe(SecurityUtils.getCurrentUserId(), ACTION_EVENT_UPDATE, RESOURCE_EVENT, STATUS_SUCCESS);
        
        return mapToResponse(updatedEvent);
    }
    
    @Override 
    public EventResponse getEventById(Long id) {
        return eventRepository.findById(id).map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, id));
    }

    @Override 
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override 
    public List<EventResponse> getEventsBySite(Long siteId) {
        return eventRepository.findBySiteId(siteId).stream().map(this::mapToResponse).toList();
    }

    @Override 
    public List<EventResponse> getEventsByProgram(Long programId) {
        return eventRepository.findByProgramId(programId).stream().map(this::mapToResponse).toList();
    }

    @Override 
    @Transactional 
    public void deleteEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException(ENTITY_NAME, eventId);
        }
        eventRepository.deleteById(eventId);
        logAuditSafe(SecurityUtils.getCurrentUserId(), ACTION_EVENT_DELETE, RESOURCE_EVENT, STATUS_SUCCESS);
    }

    @Override
    public Page<EventResponse> getEventsPaged(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (status != null && !status.isBlank()) {
            try {
                EventStatus statusEnum = EventStatus.valueOf(status.toUpperCase());
                return eventRepository.findByStatus(statusEnum, pageable).map(this::mapToResponse);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid Status Filter.");
            }
        }
        
        return eventRepository.findAll(pageable).map(this::mapToResponse); 
    }

    // --- Private Helper Methods ---

    private void logAuditSafe(Long userId, String action, String resource, String status) {
        try {
            userClient.logAction(userId, action, resource, status);
        } catch (Exception e) {
            log.error("Failed to push audit log to USER-SERVICE: {}", e.getMessage());
        }
    }

    private void sendSystemAlertSafe(Long userId, Long entityId, String subject, String message, String category) {
        try {
            notificationClient.sendSystemAlert(userId, entityId, subject, message, category);
        } catch (Exception e) {
            log.error("Failed to push system alert to NOTIFICATION-SERVICE: {}", e.getMessage());
        }
    }

    private EventResponse mapToResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setEventId(event.getEventId());
        response.setSiteId(event.getSiteId()); 
        response.setProgramId(event.getProgramId());
        response.setTitle(event.getTitle());
        response.setLocation(event.getLocation());
        response.setDate(event.getDate());
        
        if (event.getStatus() != null) {
            response.setStatus(event.getStatus().name());
        }
        
        return response;
    }
}