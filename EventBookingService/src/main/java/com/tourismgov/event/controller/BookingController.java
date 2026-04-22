package com.tourismgov.event.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.event.dto.BookingRequest;
import com.tourismgov.event.dto.BookingResponse;
import com.tourismgov.event.dto.UpdateBookingStatusRequest;
import com.tourismgov.event.service.BookingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1") 
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Validated // Required at class level for @Positive and @Min on path/query parameters
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/events/{eventId}/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @PathVariable("eventId") @Positive(message = "Event ID must be a positive number") Long eventId,
            @Valid @RequestBody BookingRequest request) {
        
        log.info("REST request to create booking for Event ID: {}", eventId);
        return new ResponseEntity<>(bookingService.createBooking(eventId, request), HttpStatus.CREATED);
    }

    @PatchMapping("/bookings/{bookingId}/status")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable("bookingId") @Positive(message = "Booking ID must be a positive number") Long bookingId,
            @Valid @RequestBody UpdateBookingStatusRequest request) { // FIX: Changed from BookingRequest
        
        log.info("REST request to update status of Booking ID: {}", bookingId);
        return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, request));
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable("bookingId") @Positive(message = "Booking ID must be a positive number") Long bookingId) {
        
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @GetMapping("/events/{eventId}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsByEvent(
            @PathVariable("eventId") @Positive(message = "Event ID must be a positive number") Long eventId) {
        
        return ResponseEntity.ok(bookingService.getBookingsByEvent(eventId));
    }

    @GetMapping("/events/{eventId}/bookings/paged")
    public ResponseEntity<Page<BookingResponse>> getBookingsByEventPaged(
            @PathVariable("eventId") @Positive(message = "Event ID must be a positive number") Long eventId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page cannot be less than 0") int page,
            @RequestParam(defaultValue = "20") @Positive(message = "Size must be a positive number") int size) {
        
        return ResponseEntity.ok(bookingService.getBookingsByEventPaged(eventId, page, size));
    }

    @GetMapping("/bookings/tourist/{touristId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByTourist(
            @PathVariable("touristId") @Positive(message = "Tourist ID must be a positive number") Long touristId) {
        
        return ResponseEntity.ok(bookingService.getBookingsByTourist(touristId));
    }

    @GetMapping("/bookings/paged")
    public ResponseEntity<Page<BookingResponse>> getAllBookingsPaged(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page cannot be less than 0") int page,
            @RequestParam(defaultValue = "20") @Positive(message = "Size must be a positive number") int size) {
        
        return ResponseEntity.ok(bookingService.getAllBookingsPaged(status, page, size));
    }
}