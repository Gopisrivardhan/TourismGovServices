package com.tourismgov.compliance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tourismgov.compliance.dto.ComplianceRecordRequestDTO;
import com.tourismgov.compliance.dto.ComplianceRecordResponseDTO;

public interface ComplianceService {
    
    // Create a new compliance check
	ComplianceRecordResponseDTO createComplianceCheck(ComplianceRecordRequestDTO request);
    
    // Fetch all compliance records with pagination
    Page<ComplianceRecordResponseDTO> getAllComplianceRecords(Pageable pageable);
    
    // Fetch a specific record by its ID
    ComplianceRecordResponseDTO getComplianceRecordById(Long recordId);
    
    // Update the result (e.g., COMPLIANT, NON_COMPLIANT) without needing the officer ID in the parameters
    ComplianceRecordResponseDTO updateComplianceResult(Long recordId, String result);
    
    // Delete a compliance record
    void deleteComplianceRecord(Long recordId);
}