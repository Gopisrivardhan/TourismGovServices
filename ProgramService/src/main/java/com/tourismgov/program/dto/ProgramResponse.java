package com.tourismgov.program.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class ProgramResponse {
    private Long programId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double budget;
    private String status;
    private List<ResourceResponse> resources;
}