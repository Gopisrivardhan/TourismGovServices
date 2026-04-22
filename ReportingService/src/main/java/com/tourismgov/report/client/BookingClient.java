package com.tourismgov.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.tourismgov.report.dto.BookingDTO;

import java.util.List;

@FeignClient(name = "tourismgov-app")
public interface BookingClient {
    @GetMapping("/tourismgov/v1/bookings")
    List<BookingDTO> getAllBookings();
}
