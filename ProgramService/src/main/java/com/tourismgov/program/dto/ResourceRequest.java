package com.tourismgov.program.dto;

import com.tourismgov.program.enums.ResourceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourceRequest {
    @NotBlank(message = "Resource type (Funds/Venue/Staff) is required")
    private ResourceType type;
    
    @NotNull(message = "Quantity is required")
    private Double quantity;
}