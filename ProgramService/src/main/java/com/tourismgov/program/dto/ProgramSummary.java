package com.tourismgov.program.dto;

import com.tourismgov.program.enums.ResourceStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProgramSummary {
    private Long programId;
    private String title;
    private ResourceStatus status;
}