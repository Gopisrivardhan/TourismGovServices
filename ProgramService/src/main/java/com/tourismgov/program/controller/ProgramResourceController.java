package com.tourismgov.program.controller;

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

import com.tourismgov.program.dto.ResourceRequest;
import com.tourismgov.program.dto.ResourceResponse;
import com.tourismgov.program.enums.ResourceStatus;
import com.tourismgov.program.service.ResourceService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/resources")
@CrossOrigin(origins = "*") // Allows API Gateway and Frontend to access
@RequiredArgsConstructor
@Validated // Required to trigger @Positive validation on path variables
public class ProgramResourceController {

    private final ResourceService resourceService;

    /**
     * Allocates a resource directly to a program.
     * POST /tourismgov/v1/resources/program/{programId}
     */
    @PostMapping("/program/{programId}")
    public ResponseEntity<ResourceResponse> allocateResource(
            @PathVariable("programId") @Positive(message = "Program ID must be positive") Long programId,
            @Valid @RequestBody ResourceRequest request) {
        
        log.info("REST request to allocate {} resource to Program ID: {}", request.getType(), programId);
        return new ResponseEntity<>(resourceService.allocateResourceToProgram(programId, request), HttpStatus.CREATED);
    }

    /**
     * Retrieves all resources for a specific program.
     * GET /tourismgov/v1/resources/program/{programId}
     */
    @GetMapping("/program/{programId}")
    public ResponseEntity<List<ResourceResponse>> getResourcesForProgram(
            @PathVariable("programId") @Positive(message = "Program ID must be positive") Long programId) {
        
        log.info("REST request to get resources for Program ID: {}", programId);
        return ResponseEntity.ok(resourceService.getResourcesByProgram(programId));
    }

    /**
     * Updates the status of a specific resource.
     * PATCH /tourismgov/v1/resources/{resourceId}/status?status=EXPENDED
     */
    @PatchMapping("/{resourceId}/status")
    public ResponseEntity<ResourceResponse> updateResourceStatus(
            @PathVariable("resourceId") @Positive(message = "Resource ID must be positive") Long resourceId,
            @RequestParam("status") @NotNull(message = "Status is required") ResourceStatus status) {
        
        // Note: Spring automatically converts the String query param (e.g. ?status=EXPENDED) 
        // into your ResourceStatus Enum! If an invalid string is passed, Spring handles the error.
        log.info("REST request to update status of Resource ID: {} to {}", resourceId, status);
        return ResponseEntity.ok(resourceService.updateResourceStatus(resourceId, status));
    }

    /**
     * Deletes a resource.
     * DELETE /tourismgov/v1/resources/{resourceId}
     */
    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable("resourceId") @Positive(message = "Resource ID must be positive") Long resourceId) {
        
        log.info("REST request to delete Resource ID: {}", resourceId);
        resourceService.deleteResource(resourceId);
        return ResponseEntity.noContent().build(); // 204 No Content is the standard for successful deletion
    }
}