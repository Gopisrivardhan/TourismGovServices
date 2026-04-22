package com.tourismgov.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequest {
    
    @NotNull(message = "Tourist ID is required")
    private Long touristId;
    
    @Min(value = 1, message = "You must book at least 1 ticket")
    private Integer numberOfTickets;
}