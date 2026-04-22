package com.tourismgov.report.dto;

import com.tourismgov.report.enums.ReportScope;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequestDTO {
    @NotNull(message = "Scope is required")
    private ReportScope scope;

    @NotNull(message = "Requester ID is required")
    private Long requesterId;
}
