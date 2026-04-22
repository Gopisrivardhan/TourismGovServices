package com.tourismgov.compliance.dto;

import java.time.LocalDateTime;

import com.tourismgov.compliance.enums.AuditStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuditRequestDTO {

    @NotBlank(message = "Audit scope cannot be empty")
    @Size(max = 100, message = "Scope cannot exceed 100 characters")
    private String scope;

    @Size(max = 2000, message = "Findings cannot exceed 2000 characters")
    private String findings; 

    @NotNull(message = "Audit date is required")
    @PastOrPresent(message = "Audit date cannot be in the future")
    private LocalDateTime date;

    // Optional on creation, defaults to PLANNED in the service layer if null
    private AuditStatus status;
}