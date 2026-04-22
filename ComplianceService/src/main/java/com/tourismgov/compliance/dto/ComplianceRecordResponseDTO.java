package com.tourismgov.compliance.dto;

import java.time.LocalDateTime;

import com.tourismgov.compliance.enums.ComplianceResult;
import com.tourismgov.compliance.enums.ComplianceType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComplianceRecordResponseDTO {
    private Long complianceId;
    private String referenceNumber;
    private Long entityId;
    private ComplianceType type;
    private ComplianceResult result;
    private LocalDateTime date;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}