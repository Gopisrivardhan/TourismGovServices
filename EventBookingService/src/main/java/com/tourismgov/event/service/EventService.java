package com.tourismgov.event.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.tourismgov.event.dto.CreateEventRequest;
import com.tourismgov.event.dto.EventResponse;
import com.tourismgov.event.dto.UpdateEventStatusRequest;

public interface EventService {
    
    EventResponse createEvent(CreateEventRequest request);
    
    Page<EventResponse> getEventsPaged(String status, int page, int size);
    
    List<EventResponse> getAllEvents();
    
    EventResponse getEventById(Long eventId);
    
    List<EventResponse> getEventsBySite(Long siteId);
    
    List<EventResponse> getEventsByProgram(Long programId);
    
    EventResponse updateEvent(Long eventId, CreateEventRequest request);
    
    EventResponse updateEventStatus(Long eventId, UpdateEventStatusRequest request);
    
    void deleteEvent(Long eventId);
    
}