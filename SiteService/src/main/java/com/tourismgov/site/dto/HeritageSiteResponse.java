package com.tourismgov.site.dto;

import lombok.Data;
import java.util.List;

@Data
public class HeritageSiteResponse {
    private Long siteId;
    private String name;
    private String location;
    private String description;
    private String status;
    private List<PreservationActivityResponse> preservationActivities;
}