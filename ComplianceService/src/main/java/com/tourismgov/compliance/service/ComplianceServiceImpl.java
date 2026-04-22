package com.tourismgov.compliance.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.compliance.client.UserClient;
import com.tourismgov.compliance.dto.ComplianceRecordRequestDTO;
import com.tourismgov.compliance.dto.ComplianceRecordResponseDTO;
import com.tourismgov.compliance.entity.ComplianceRecord;
import com.tourismgov.compliance.enums.ComplianceResult;
import com.tourismgov.compliance.exceptions.ResourceNotFoundException;
import com.tourismgov.compliance.repository.ComplianceRecordRepository;
import com.tourismgov.compliance.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ComplianceServiceImpl implements ComplianceService {

    private final ComplianceRecordRepository complianceRepository;
    
    // MICROSERVICE FIX: Use Feign client instead of local monolithic service
    private final UserClient userClient;

    @Override
    @Transactional
    public ComplianceRecordResponseDTO createComplianceCheck(ComplianceRecordRequestDTO request) {
        log.info("Creating compliance record for REF: {}", request.getReferenceNumber());

        ComplianceRecord record = new ComplianceRecord();
        record.setReferenceNumber(request.getReferenceNumber());
        record.setEntityId(request.getEntityId());
        
        // THE FIX: Set Enums directly, avoid String manipulations
        record.setType(request.getType());
        record.setNotes(request.getDescription());
        record.setResult(ComplianceResult.PENDING_REVIEW);
        record.setDate(LocalDateTime.now()); // Set official inspection date

        ComplianceRecord saved = complianceRepository.save(record);

        // SECURE FIX: Track exactly who created the compliance check using local SecurityUtils
        Long officerId = SecurityUtils.getCurrentUserId();
        userClient.logAuditAction(officerId, "COMPLIANCE_CREATED", "REF_" + saved.getReferenceNumber(), "SUCCESS");

        return mapToComplianceDto(saved);
    }

    @Override
    public Page<ComplianceRecordResponseDTO> getAllComplianceRecords(Pageable pageable) {
        return complianceRepository.findAll(pageable).map(this::mapToComplianceDto);
    }

    @Override
    public ComplianceRecordResponseDTO getComplianceRecordById(Long recordId) {
        return complianceRepository.findById(recordId)
                .map(this::mapToComplianceDto)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance Record", recordId));
    }

    @Override
    @Transactional
    public ComplianceRecordResponseDTO updateComplianceResult(Long recordId, String result) {
        ComplianceRecord record = complianceRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance Record", recordId));

        try {
            // THE FIX: Safely parse the String into the strongly-typed Enum
            record.setResult(ComplianceResult.valueOf(result.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Result. Allowed: COMPLIANT, NON_COMPLIANT, PARTIALLY_COMPLIANT, PENDING_REVIEW, EXEMPT");
        }
        
        ComplianceRecord updated = complianceRepository.save(record);

        Long officerId = SecurityUtils.getCurrentUserId();
        userClient.logAuditAction(officerId, "COMPLIANCE_UPDATED", "REF_" + record.getReferenceNumber(), "SUCCESS");

        return mapToComplianceDto(updated);
    }

    @Override
    @Transactional
    public void deleteComplianceRecord(Long recordId) {
        ComplianceRecord record = complianceRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance Record", recordId));
        
        Long officerId = SecurityUtils.getCurrentUserId();
        userClient.logAuditAction(officerId, "COMPLIANCE_DELETED", "REF_" + record.getReferenceNumber(), "SUCCESS");
        
        complianceRepository.delete(record);
    }

    private ComplianceRecordResponseDTO mapToComplianceDto(ComplianceRecord r) {
        return ComplianceRecordResponseDTO.builder()
                .complianceId(r.getComplianceId())
                .referenceNumber(r.getReferenceNumber())
                .entityId(r.getEntityId())
                // THE FIX: Changed .entityType() to .type() to match your DTO
                .type(r.getType())
                .result(r.getResult())
                .date(r.getDate())
                .notes(r.getNotes())
                // Mapped the BaseEntity audit fields as well
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}