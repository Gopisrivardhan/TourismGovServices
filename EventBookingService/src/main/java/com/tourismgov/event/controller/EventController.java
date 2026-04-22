package com.tourismgov.event.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.event.dto.CreateEventRequest;
import com.tourismgov.event.dto.EventResponse;
import com.tourismgov.event.dto.UpdateEventStatusRequest;
import com.tourismgov.event.service.EventService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/events")
@CrossOrigin(origins = "*") 
@RequiredArgsConstructor
@Validated 
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request) {
        
        log.info("REST request to create event: {}", request.getTitle());
        return new ResponseEntity<>(eventService.createEvent(request), HttpStatus.CREATED);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<EventResponse>> getEventsPaged(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Positive int size) {
        
        return ResponseEntity.ok(eventService.getEventsPaged(status, page, size));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable("eventId") @Positive(message = "Event ID must be positive") Long eventId) {
        
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<EventResponse>> getEventsBySite(
            @PathVariable("siteId") @Positive(message = "Site ID must be positive") Long siteId) {
        
        return ResponseEntity.ok(eventService.getEventsBySite(siteId));
    }

    @GetMapping("/program/{programId}")
    public ResponseEntity<List<EventResponse>> getEventsByProgram(
            @PathVariable("programId") @Positive(message = "Program ID must be positive") Long programId) {
        
        return ResponseEntity.ok(eventService.getEventsByProgram(programId));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable("eventId") @Positive(message = "Event ID must be positive") Long eventId,
            @Valid @RequestBody CreateEventRequest request) {
        
        log.info("REST request to update event ID: {}", eventId);
        return ResponseEntity.ok(eventService.updateEvent(eventId, request));
    }

    @PatchMapping("/{eventId}/status")
    public ResponseEntity<EventResponse> updateEventStatus(
            @PathVariable("eventId") @Positive(message = "Event ID must be positive") Long eventId,
            @Valid @RequestBody UpdateEventStatusRequest request) {
        
        log.info("REST request to update status for event ID: {} to {}", eventId, request.getStatus());
        return ResponseEntity.ok(eventService.updateEventStatus(eventId, request));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable("eventId") @Positive(message = "Event ID must be positive") Long eventId) {
        
        log.info("REST request to delete event ID: {}", eventId);
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}