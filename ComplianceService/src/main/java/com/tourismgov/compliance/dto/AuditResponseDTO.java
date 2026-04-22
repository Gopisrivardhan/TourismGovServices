package com.tourismgov.compliance.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tourismgov.compliance.enums.AuditStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Ensures null fields (like findings on a new audit) aren't sent in the JSON response
public class AuditResponseDTO {

    private Long auditId;
    
    // In a microservice, we send the flat ID and the resolved name, rather than a nested User object
    private Long officerId;
    private String officerName; 
    
    private String scope;
    private String findings;
    private LocalDateTime date;
    
    // Storing as a String in the DTO makes JSON serialization straightforward, 
    // even though it maps to the AuditStatus enum in the database.
    private AuditStatus status; 

}