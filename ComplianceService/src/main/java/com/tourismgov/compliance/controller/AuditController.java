package com.tourismgov.compliance.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.compliance.dto.AuditRequestDTO;
import com.tourismgov.compliance.dto.AuditResponseDTO;
import com.tourismgov.compliance.dto.AuditUpdateRequestDTO;
import com.tourismgov.compliance.service.AuditService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/audits")
@CrossOrigin(origins = "*") 
@RequiredArgsConstructor
@Validated // REQUIRED: Tells Spring to validate the @PathVariable and @RequestParam variables
public class AuditController {

    private final AuditService auditService;

    /**
     * Create a new official audit.
     */
    @PostMapping("/official")
    public ResponseEntity<AuditResponseDTO> recordOfficialAudit(
            @Valid @RequestBody AuditRequestDTO request) {
        
        log.info("REST request to record official audit");
        AuditResponseDTO savedAudit = auditService.recordAudit(request);
        
        return new ResponseEntity<>(savedAudit, HttpStatus.CREATED);
    }

    /**
     * Fetch all official audits.
     */
    @GetMapping("/official")
    public ResponseEntity<List<AuditResponseDTO>> getAllOfficialAudits() {
        
        log.info("REST request to get all official audits");
        return ResponseEntity.ok(auditService.getAllAudits());
    }

    /**
     * Fetch all audits performed by a specific officer, with pagination.
     * Uses @Positive to ensure no one passes a negative ID.
     */
    @GetMapping("/official/officer/{officerId}")
    public ResponseEntity<Page<AuditResponseDTO>> getAuditsByOfficer(
            @PathVariable("officerId") @Positive(message = "Officer ID must be positive") Long officerId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Positive int size) {
        
        log.info("REST request to get paginated audits for officer ID: {}", officerId);
        return ResponseEntity.ok(auditService.getAuditsByOfficer(officerId, page, size));
    }

    /**
     * Update the findings or status of an existing audit.
     * Uses the new AuditUpdateRequestDTO so @Valid actually works!
     */
    @PatchMapping("/official/{id}")
    public ResponseEntity<AuditResponseDTO> updateAuditFindings(
            @PathVariable("id") @Positive(message = "Audit ID must be positive") Long auditId,
            @Valid @RequestBody AuditUpdateRequestDTO updates) {
        
        log.info("REST request to update findings/status for audit ID: {}", auditId);
        
        // Extract the validated strings from the DTO
        String findings = updates.getFindings();
        String status = updates.getStatus();
        
        AuditResponseDTO updatedAudit = auditService.updateAuditFindings(auditId, findings, status);
        
        return ResponseEntity.ok(updatedAudit);
    }
}