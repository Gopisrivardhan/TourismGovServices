package com.tourismgov.event.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@RequiredArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private Long touristId;
    private Long eventId;
    private LocalDateTime date;
    private String status;
    private Integer numberOfTickets;
}