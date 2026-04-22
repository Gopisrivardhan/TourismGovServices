package com.tourismgov.event.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventResponse {
    private Long eventId;
    private Long siteId;
    private Long programId;
    private String title;
    private String location;
    private LocalDateTime date;
    private String status;
}