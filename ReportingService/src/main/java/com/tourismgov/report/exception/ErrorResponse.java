package com.tourismgov.report.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ErrorResponse - Standardized JSON structure for all API errors.
 * Using Lombok to keep it clean and consistent with the rest of the microservice.
 */
@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class ErrorResponse {

    // 1. Ensures the date is readable (ISO-8601) in Postman and Frontend
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;
    private String error;
    private String message;
}