package com.tourismgov.program.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogRequest {
    private Long userId;
    private String action;
    private String resource;
    private String status; 
}