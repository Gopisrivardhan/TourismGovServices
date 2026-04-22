package com.tourismgov.report.service;

import com.tourismgov.report.dto.DashboardDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public interface DashboardService {

  
    DashboardDTO getDashboardMetrics(
        @NotBlank(message = "Role is required") String role, 
        @NotNull(message = "User ID is required") Long userId
    );
}
