package com.tourismgov.program.dto;

import com.tourismgov.program.enums.ResourceType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourceRequest {
	@NotNull(message = "Resource type cannot be null")
    private ResourceType type;
    
    @NotNull(message = "Quantity is required")
    private Double quantity;
}