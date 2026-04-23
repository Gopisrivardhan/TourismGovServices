package com.tourismgov.event.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.event.client.NotificationClient;
import com.tourismgov.event.client.ProgramClient;
import com.tourismgov.event.client.SiteClient;
import com.tourismgov.event.client.UserClient;
import com.tourismgov.event.dto.AuditLogRequest;
import com.tourismgov.event.dto.CreateEventRequest;
import com.tourismgov.event.dto.EventResponse;
import com.tourismgov.event.dto.ProgramDto;
import com.tourismgov.event.dto.UpdateEventStatusRequest;
import com.tourismgov.event.entity.Event;
import com.tourismgov.event.enums.EventStatus;
import com.tourismgov.event.exceptions.ErrorMessages;
import com.tourismgov.event.exceptions.ResourceNotFoundException;
import com.tourismgov.event.repository.EventRepository;
import com.tourismgov.event.security.SecurityUtils;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final String RESOURCE_EVENT = "EventService";
    private static final String ENTITY_NAME = "Event";
    private static final String ENTITY_SITE = "Heritage Site";
    private static final String ENTITY_PROGRAM = "Tourism Program";
    
    private static final String ACTION_EVENT_CREATE = "EVENT_CREATE";
    private static final String ACTION_EVENT_UPDATE = "EVENT_UPDATE";
    private static final String ACTION_EVENT_STATUS_UPDATE = "EVENT_STATUS_UPDATE";
    private static final String ACTION_EVENT_DELETE = "EVENT_DELETE";
    
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final EventRepository eventRepository;
    private final UserClient userClient; 
    private final NotificationClient notificationClient;
    private final SiteClient siteClient;       
    private final ProgramClient programClient; 

    @Override
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Creating event: {}", request.getTitle());
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Check for Duplicate Event
        if (eventRepository.existsByTitleAndSiteIdAndDate(request.getTitle(), request.getSiteId(), request.getDate())) {
            log.warn("Duplicate event creation attempt blocked for title: {}", request.getTitle());
            logAuditSafe(currentUserId, ACTION_EVENT_CREATE, RESOURCE_EVENT, STATUS_FAILED);
            throw new IllegalStateException("An event with this title is already scheduled at this site for the given date.");
        }

        // 2. Validate Site and Program externally
        validateSiteAndProgram(request.getSiteId(), request.getProgramId(), request.getDate());

        // 3. Save Event
        Event event = new Event();
        event.setSiteId(request.getSiteId()); 
        event.setTitle(request.getTitle());
        event.setLocation(request.getLocation());
        event.setDate(request.getDate());
        
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        } else {
            event.setStatus(EventStatus.SCHEDULED);
        }

        if (request.getProgramId() != null) {
            event.setProgramId(request.getProgramId());
        }

        Event saved = eventRepository.save(event);
        
        // 4. Triggers
        logAuditSafe(currentUserId, ACTION_EVENT_CREATE, RESOURCE_EVENT, STATUS_SUCCESS);
                
        String message = String.format("A new event '%s' has been scheduled at %s on %s.", 
                saved.getTitle(), saved.getLocation(), saved.getDate().toLocalDate());
                
        sendSystemAlertSafe(currentUserId, saved.getEventId(), "New Event Scheduled!", message, "EVENT");

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long eventId, CreateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, eventId));
        
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Prevent updating to a duplicate of ANOTHER event
        if (eventRepository.existsByTitleAndSiteIdAndDateAndEventIdNot(
                request.getTitle(), request.getSiteId(), request.getDate(), eventId)) {
            log.warn("Update blocked: Conflicts with an existing event title: {}", request.getTitle());
            logAuditSafe(currentUserId, ACTION_EVENT_UPDATE, RESOURCE_EVENT, STATUS_FAILED);
            throw new IllegalStateException("Another event with this title is already scheduled at this site for the given date.");
        }

        // 2. Check if the Site, Program, or Date actually changed before calling other microservices
        boolean needsValidation = !event.getSiteId().equals(request.getSiteId()) || 
                                  !event.getDate().equals(request.getDate()) ||
                                  (request.getProgramId() != null && !request.getProgramId().equals(event.getProgramId()));

        if (needsValidation) {
            validateSiteAndProgram(request.getSiteId(), request.getProgramId(), request.getDate());
        }

        // 3. Update fields
        event.setTitle(request.getTitle());
        event.setLocation(request.getLocation());
        event.setDate(request.getDate());
        event.setSiteId(request.getSiteId()); 

        if (request.getProgramId() != null) {
            event.setProgramId(request.getProgramId());
        }

        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }
        
        Event updatedEvent = eventRepository.save(event);
        
        // 4. Audit Log
        logAuditSafe(currentUserId, ACTION_EVENT_UPDATE, RESOURCE_EVENT, STATUS_SUCCESS);
        
        return mapToResponse(updatedEvent);
    }
    
    @Override
    @Transactional
    public EventResponse updateEventStatus(Long eventId, UpdateEventStatusRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, eventId));
        
        EventStatus oldStatus = event.getStatus();
        
        if (request.getStatus() != null) {
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
    public void cancelEventsByProgram(Long programId) {
        log.info(">>>> [EVENT-SERVICE] RECEIVED CANCELLATION REQUEST FOR PROGRAM ID: {}", programId);

        // 1. Fetch events
        List<Event> events = eventRepository.findByProgramId(programId);

        // 2. CHECK: Is the list empty?
        if (events == null || events.isEmpty()) {
            log.error(">>>> [EVENT-SERVICE] FAILURE: No events found linked to Program ID: {}. Check your database 'program_id' column!", programId);
            return;
        }

        log.info(">>>> [EVENT-SERVICE] SUCCESS: Found {} events. Updating status now...", events.size());

        // 3. Update and Save
        events.forEach(event -> {
            log.info(">>>> [EVENT-SERVICE] Cancelling Event: {}", event.getTitle());
            event.setStatus(EventStatus.CANCELLED);
        });

        eventRepository.saveAll(events);
        log.info(">>>> [EVENT-SERVICE] ALL EVENTS SUCCESSFULLY CANCELLED IN DATABASE.");
    }
    
    @Override
    public EventResponse getEventById(Long eventId) {
        return eventRepository.findById(eventId).map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME, eventId));
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
    public Page<EventResponse> getEventsPaged(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (status != null && !status.isBlank()) {
            try {
                EventStatus statusEnum = EventStatus.valueOf(status.toUpperCase());
                return eventRepository.findByStatus(statusEnum, pageable).map(this::mapToResponse);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(ErrorMessages.INVALID_STATUS);
            }
        }
        
        return eventRepository.findAll(pageable).map(this::mapToResponse); 
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

    // --- Private Helper Methods ---

    private void validateSiteAndProgram(Long siteId, Long programId, LocalDateTime eventDate) {
        if (siteId != null) {
            try {
                siteClient.getSiteById(siteId);
            } catch (FeignException.NotFound e) {
                throw new ResourceNotFoundException(ENTITY_SITE, siteId);
            } catch (Exception e) {
                throw new RuntimeException(ErrorMessages.SITE_SERVICE_ERROR, e);
            }
        }

        if (programId != null) {
            try {
                ProgramDto program = programClient.getProgramById(programId);
                
                if (eventDate != null) {
                    LocalDate eDate = eventDate.toLocalDate();
                    if (eDate.isBefore(program.getStartDate()) || eDate.isAfter(program.getEndDate())) {
                        throw new IllegalArgumentException(
                            String.format(ErrorMessages.EVENT_DATE_OUT_OF_BOUNDS,
                            eDate, program.getStartDate(), program.getEndDate())
                        );
                    }
                }
            } catch (FeignException.NotFound e) {
                throw new ResourceNotFoundException(ENTITY_PROGRAM, programId);
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    throw e;
                }
                throw new RuntimeException(ErrorMessages.PROGRAM_SERVICE_ERROR, e);
            }
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

    // --- Single, Correct Audit Log Helper ---
    private void logAuditSafe(Long userId, String action, String resource, String status) {
        try {
            // Instantiate the DTO and set the 4 required fields
            AuditLogRequest auditRequest = new AuditLogRequest();
            auditRequest.setUserId(userId);
            auditRequest.setAction(action);
            auditRequest.setResource(resource);
            auditRequest.setStatus(status);
            
            // Push to the USER-SERVICE
            userClient.logAction(auditRequest);
            
        } catch (Exception e) {
            log.error("Failed to push audit log to USER-SERVICE: {}", e.getMessage());
        }
    }
}