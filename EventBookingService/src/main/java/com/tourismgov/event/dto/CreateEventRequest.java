package com.tourismgov.event.dto;

import java.time.LocalDateTime;

import com.tourismgov.event.enums.EventStatus;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateEventRequest {
    @NotNull(message = "Site ID is required")
    private Long siteId;

    @NotBlank(message = "Title is required")
    private String title;
    private String location;
    
    @NotNull(message = "Date is required")
    private LocalDateTime date;
    private Long programId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EventStatus status;
}