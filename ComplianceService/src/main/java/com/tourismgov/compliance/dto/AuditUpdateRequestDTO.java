package com.tourismgov.compliance.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuditUpdateRequestDTO {

    @Size(max = 2000, message = "Findings cannot exceed 2000 characters")
    private String findings;

    // Ensures the status exactly matches your Enum values
    @Pattern(regexp = "^(PLANNED|IN_PROGRESS|UNDER_REVIEW|COMPLETED|CANCELLED)$", 
             message = "Invalid status. Allowed: PLANNED, IN_PROGRESS, UNDER_REVIEW, COMPLETED, CANCELLED")
    private String status;
}