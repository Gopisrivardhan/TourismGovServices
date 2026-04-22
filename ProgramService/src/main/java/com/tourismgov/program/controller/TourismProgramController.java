package com.tourismgov.program.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.program.dto.ProgramRequest;
import com.tourismgov.program.dto.ProgramResponse;
import com.tourismgov.program.enums.ProgramStatus;
import com.tourismgov.program.service.TourismProgramService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/programs")
@CrossOrigin(origins = "*") // Allows API Gateway and Frontend to access this service
@RequiredArgsConstructor
@Validated // Required to trigger @Positive validation on path and request parameters
public class TourismProgramController {

    private final TourismProgramService programService;

    /**
     * Creates a new Tourism Program.
     * POST /tourismgov/v1/programs
     */
    @PostMapping
    public ResponseEntity<ProgramResponse> createProgram(@Valid @RequestBody ProgramRequest request) {
        log.info("REST request to create Tourism Program: '{}'", request.getTitle());
        return new ResponseEntity<>(programService.createProgram(request), HttpStatus.CREATED);
    }

    /**
     * Retrieves a single Tourism Program by ID.
     * GET /tourismgov/v1/programs/{programId}
     */
    @GetMapping("/{programId}")
    public ResponseEntity<ProgramResponse> getProgramById(
            @PathVariable("programId") @Positive(message = "Program ID must be positive") Long programId) {
        log.info("REST request to fetch Program ID: {}", programId);
        return ResponseEntity.ok(programService.getProgramById(programId));
    }

    /**
     * Retrieves all Tourism Programs (Unpaged).
     * GET /tourismgov/v1/programs
     */
    @GetMapping
    public ResponseEntity<List<ProgramResponse>> getAllPrograms() {
        log.info("REST request to fetch all Tourism Programs");
        return ResponseEntity.ok(programService.getAllPrograms());
    }

    /**
     * Retrieves a paginated list of Tourism Programs.
     * GET /tourismgov/v1/programs/paged?page=0&size=10
     */
    @GetMapping("/paged")
    public ResponseEntity<Page<ProgramResponse>> getProgramsPaged(
            @RequestParam(defaultValue = "0") @PositiveOrZero(message = "Page index must not be negative") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Page size must be greater than zero") int size) {
        log.info("REST request to fetch Programs (Paged) - Page: {}, Size: {}", page, size);
        return ResponseEntity.ok(programService.getProgramsPaged(page, size));
    }

    /**
     * Updates an existing Tourism Program entirely.
     * PUT /tourismgov/v1/programs/{programId}
     */
    @PutMapping("/{programId}")
    public ResponseEntity<ProgramResponse> updateProgram(
            @PathVariable("programId") @Positive(message = "Program ID must be positive") Long programId, 
            @Valid @RequestBody ProgramRequest request) {
        log.info("REST request to update Program ID: {}", programId);
        return ResponseEntity.ok(programService.updateProgram(programId, request));
    }

    /**
     * Updates only the status of a Tourism Program.
     * PATCH /tourismgov/v1/programs/{programId}/status?status=ACTIVE
     */
    @PatchMapping("/{programId}/status")
    public ResponseEntity<ProgramResponse> updateProgramStatus(
            @PathVariable("programId") @Positive(message = "Program ID must be positive") Long programId,
            @RequestParam("status") @NotNull(message = "Status is required") ProgramStatus status) {
        
        // Spring safely converts the string from the URL to your ProgramStatus Enum.
        // We pass status.name() to your service implementation which currently accepts a String.
        log.info("REST request to update status of Program ID: {} to {}", programId, status);
        return ResponseEntity.ok(programService.updateProgramStatus(programId, status.name()));
    }

    /**
     * Deletes a Tourism Program.
     * DELETE /tourismgov/v1/programs/{programId}
     */
    @DeleteMapping("/{programId}")
    public ResponseEntity<Void> deleteProgram(
            @PathVariable("programId") @Positive(message = "Program ID must be positive") Long programId) {
        log.info("REST request to delete Program ID: {}", programId);
        programService.deleteProgram(programId);
        return ResponseEntity.noContent().build(); // 204 No Content is standard for DELETE
    }

    /**
     * Reporting API: Generates financial health status for a program.
     * GET /tourismgov/v1/programs/{programId}/budget-report
     */
    @GetMapping("/{programId}/budget-report")
    public ResponseEntity<Map<String, Object>> getProgramBudgetReport(
            @PathVariable("programId") @Positive(message = "Program ID must be positive") Long programId) {
        log.info("REST request to generate budget report for Program ID: {}", programId);
        return ResponseEntity.ok(programService.getBudgetReport(programId));
    }
}