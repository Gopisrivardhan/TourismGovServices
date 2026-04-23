package com.tourismgov.event.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ProgramDto {
    private Long programId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
}