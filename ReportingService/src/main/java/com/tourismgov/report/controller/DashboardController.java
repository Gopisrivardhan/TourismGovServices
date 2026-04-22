package com.tourismgov.report.controller;

import com.tourismgov.report.dto.DashboardDTO;
import com.tourismgov.report.service.DashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Fetch dashboard stats. 
     * Identifies the user via headers injected by the API Gateway.
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardDTO> getDashboardStats(
            @RequestHeader("X-User-Role") String role, 
            @RequestHeader("X-User-Id") Long userId) {
        
        return ResponseEntity.ok(dashboardService.getDashboardMetrics(role, userId));
    }
}