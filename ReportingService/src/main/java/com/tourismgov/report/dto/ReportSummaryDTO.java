package com.tourismgov.report.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

import com.tourismgov.report.enums.ReportScope;

@Data
@Builder
public class ReportSummaryDTO {
    private Long reportId;
    private ReportScope scope;
    private LocalDateTime generatedDate;
    private String generatedByName;
    
    // ADD THESE THREE FIELDS
    private Long generatedByUserId; 
    private String generatedByRole;
    private String metrics; 
}
