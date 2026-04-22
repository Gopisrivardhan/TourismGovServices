package com.tourismgov.program.dto;

import com.tourismgov.program.enums.ResourceStatus;
import com.tourismgov.program.enums.ResourceType;

import lombok.Data;

@Data
public class ResourceResponse {
    private Long resourceId;
    private Long programId;
    private ResourceType type;
    private Double quantity;
    private ResourceStatus status;
}