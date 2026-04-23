package com.tourismgov.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PreservationActivityRequest {

    @NotBlank(message = "Description of the preservation work is required")
    private String description;

    @NotNull(message = "Activity date is mandatory")
    private LocalDateTime date;

    private String status;

	private boolean isRequiresSiteClosure;
}