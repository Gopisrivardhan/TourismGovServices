package com.tourismgov.event.dto;

import com.tourismgov.event.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookingStatusRequest {
    
    @NotNull(message = "Booking status cannot be null")
    private BookingStatus status; // Validates against Enum automatically!
}