package com.tourismgov.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HeritageSiteRequest {
    
    @NotBlank(message = "Site name is required")
    @Size(min = 3, max = 100, message = "Site name must be between 3 and 100 characters")
    private String name;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String status;
}