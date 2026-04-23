package com.tourismgov.site.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PreservationActivityResponse {
    private Long activityId;
    private Long siteId;
    private Long officerId;
    private String description;
    private LocalDateTime date;
    private String status;
}