package com.tourismgov.site.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.site.dto.PreservationActivityRequest;
import com.tourismgov.site.dto.PreservationActivityResponse;
import com.tourismgov.site.service.PreservationActivityService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/preservation")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Validated
public class PreservationActivityController {

    private final PreservationActivityService preservationService;

    /**
     * Log a new preservation activity for a specific site.
     * POST /tourismgov/v1/preservation/site/{siteId}
     */
    @PostMapping("/site/{siteId}")
    public ResponseEntity<PreservationActivityResponse> logActivity(
            @PathVariable("siteId") @Positive(message = "Site ID must be positive") Long siteId,
            @Valid @RequestBody PreservationActivityRequest request) {
        
        log.info("REST request to log activity for Site ID: {}", siteId);
        return new ResponseEntity<>(preservationService.logActivity(siteId, request), HttpStatus.CREATED);
    }

    /**
     * Update only the status of a specific activity.
     * PATCH /tourismgov/v1/preservation/{activityId}/status?status=COMPLETED
     */
    @PatchMapping("/{activityId}/status")
    public ResponseEntity<PreservationActivityResponse> updateStatus(
            @PathVariable("activityId") @Positive(message = "Activity ID must be positive") Long activityId,
            @RequestParam @NotBlank(message = "Status is required") String status) {
        
        log.info("REST request to update status for Activity ID: {} to {}", activityId, status);
        return ResponseEntity.ok(preservationService.updateActivityStatus(activityId, status));
    }

    /**
     * Get details of a single preservation activity.
     */
    @GetMapping("/{activityId}")
    public ResponseEntity<PreservationActivityResponse> getActivityById(
            @PathVariable("activityId") @Positive(message = "Activity ID must be positive") Long activityId) {
        
        return ResponseEntity.ok(preservationService.getActivityById(activityId));
    }

    /**
     * Get all preservation activities for a specific Heritage Site.
     */
    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<PreservationActivityResponse>> getActivitiesBySite(
            @PathVariable("siteId") @Positive(message = "Site ID must be positive") Long siteId) {
        
        return ResponseEntity.ok(preservationService.getActivitiesBySite(siteId));
    }

    /**
     * Get all activities performed by a specific officer.
     */
    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<PreservationActivityResponse>> getActivitiesByOfficer(
            @PathVariable("officerId") @Positive(message = "Officer ID must be positive") Long officerId) {
        
        return ResponseEntity.ok(preservationService.getActivitiesByOfficer(officerId));
    }

    /**
     * Delete an activity record.
     */
    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(
            @PathVariable("activityId") @Positive(message = "Activity ID must be positive") Long activityId) {
        
        log.info("REST request to delete Activity ID: {}", activityId);
        preservationService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }
}