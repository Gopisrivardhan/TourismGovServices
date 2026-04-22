package com.tourismgov.compliance.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.tourismgov.compliance.dto.ComplianceRecordRequestDTO;
import com.tourismgov.compliance.dto.ComplianceRecordResponseDTO;
import com.tourismgov.compliance.service.ComplianceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/compliance/records")
@CrossOrigin(origins = "*") // Allows the frontend to communicate with this microservice
@RequiredArgsConstructor
public class ComplianceController {

    private final ComplianceService complianceService;

    /**
     * Create a new official compliance check.
     * The @Valid annotation triggers the rules inside ComplianceRecordRequestDTO.
     */
    @PostMapping
    public ResponseEntity<ComplianceRecordResponseDTO> logComplianceRecord(
            @Valid @RequestBody ComplianceRecordRequestDTO request) {
        
        log.info("REST request to log compliance record for REF: {}", request.getReferenceNumber());
        ComplianceRecordResponseDTO savedRecord = complianceService.createComplianceCheck(request);
        
        return new ResponseEntity<>(savedRecord, HttpStatus.CREATED);
    }

    /**
     * Fetch all compliance records with pagination.
     * e.g., /tourismgov/v1/compliance/records?page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<Page<ComplianceRecordResponseDTO>> getComplianceRegister(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        log.info("REST request to get paginated compliance records");
        return ResponseEntity.ok(complianceService.getAllComplianceRecords(pageable));
    }

    /**
     * Fetch a specific compliance record by its ID.
     */
    @GetMapping("/{recordId}")
    public ResponseEntity<ComplianceRecordResponseDTO> getComplianceRecordById(
            @PathVariable Long recordId) {
        
        log.info("REST request to get compliance record by ID: {}", recordId);
        return ResponseEntity.ok(complianceService.getComplianceRecordById(recordId));
    }

    /**
     * Update only the result of a compliance record (e.g., changing to COMPLIANT or NON_COMPLIANT).
     * Uses PATCH because we are doing a partial update, not replacing the whole record.
     */
    @PatchMapping("/{recordId}/result")
    public ResponseEntity<ComplianceRecordResponseDTO> updateComplianceResult(
            @PathVariable Long recordId,
            @RequestParam("result") String result) {
        
        log.info("REST request to update compliance result for record ID: {} to {}", recordId, result);
        ComplianceRecordResponseDTO updatedRecord = complianceService.updateComplianceResult(recordId, result);
        
        return ResponseEntity.ok(updatedRecord);
    }

    /**
     * Delete a compliance record.
     * Returns 204 NO CONTENT as per REST standards for a successful deletion.
     */
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteComplianceRecord(@PathVariable Long recordId) {
        
        log.info("REST request to delete compliance record ID: {}", recordId);
        complianceService.deleteComplianceRecord(recordId);
        
        return ResponseEntity.noContent().build(); 
    }
}