package com.tourismgov.event.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEventStatusRequest {

    @NotBlank(message = "Event status cannot be blank")
    private String status;
    
}