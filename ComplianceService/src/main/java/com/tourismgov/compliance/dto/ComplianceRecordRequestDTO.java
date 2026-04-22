package com.tourismgov.compliance.dto;

import java.time.LocalDateTime;

import com.tourismgov.compliance.enums.ComplianceResult;
import com.tourismgov.compliance.enums.ComplianceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComplianceRecordRequestDTO {

    @NotBlank(message = "Reference number is required")
    @Size(max = 50, message = "Reference number cannot exceed 50 characters")
    private String referenceNumber;

    @NotNull(message = "Entity ID is required (e.g., the Site ID or Program ID)")
    private Long entityId;

    @NotNull(message = "Compliance type is required")
    private ComplianceType type;

    @NotNull(message = "Compliance result is required")
    private ComplianceResult result;

    @NotNull(message = "Date of compliance check is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDateTime date;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String description;
}