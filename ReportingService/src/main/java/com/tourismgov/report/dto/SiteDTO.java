package com.tourismgov.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteDTO {
    private Long siteId;
    private String name;
    private String location;
    private String description;
    private String status;
}
