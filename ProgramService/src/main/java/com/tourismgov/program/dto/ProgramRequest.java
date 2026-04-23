package com.tourismgov.program.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProgramRequest {
    @NotBlank(message = "Program title is required")
    private String title;

    private String description;

    @NotNull(message = "Start date is mandatory")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDate startDate;

    @NotNull(message = "End date is mandatory")
    private LocalDate endDate;

    private Double budget;
}