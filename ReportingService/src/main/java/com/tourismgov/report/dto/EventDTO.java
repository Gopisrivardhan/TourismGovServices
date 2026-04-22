package com.tourismgov.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDTO {
    private Long eventId;
    private Long siteId;
    private String title;
    private String location;
    private LocalDateTime date;
    private String status;
}
