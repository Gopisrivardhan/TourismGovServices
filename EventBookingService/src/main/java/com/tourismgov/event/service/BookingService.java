package com.tourismgov.event.service;

import java.util.List;
import org.springframework.data.domain.Page;
import com.tourismgov.event.dto.BookingRequest;
import com.tourismgov.event.dto.BookingResponse;
import com.tourismgov.event.dto.UpdateBookingStatusRequest;

public interface BookingService {
    BookingResponse createBooking(Long eventId, BookingRequest request);
    BookingResponse updateBookingStatus(Long bookingId, UpdateBookingStatusRequest request);
    BookingResponse getBookingById(Long bookingId);
    List<BookingResponse> getBookingsByEvent(Long eventId);
    List<BookingResponse> getBookingsByTourist(Long touristId);
    Page<BookingResponse> getAllBookingsPaged(String status, int page, int size);
    Page<BookingResponse> getBookingsByEventPaged(Long eventId, int page, int size);
}