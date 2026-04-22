package com.tourismgov.compliance.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.compliance.client.UserClient;
import com.tourismgov.compliance.dto.AuditRequestDTO;
import com.tourismgov.compliance.dto.AuditResponseDTO;
import com.tourismgov.compliance.entity.Audit;
import com.tourismgov.compliance.enums.AuditStatus;
import com.tourismgov.compliance.exceptions.ResourceNotFoundException;
import com.tourismgov.compliance.repository.AuditRepository;
import com.tourismgov.compliance.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;
    private final UserClient userClient; 

    @Override
    @Transactional
    public AuditResponseDTO recordAudit(AuditRequestDTO dto) {
        log.info("Recording official audit for scope: {}", dto.getScope());
        Long officerId = SecurityUtils.getCurrentUserId();

        Audit audit = new Audit();
        audit.setOfficerId(officerId); 
        audit.setScope(dto.getScope());
        audit.setFindings(dto.getFindings());
        audit.setDate(dto.getDate());
        
        // THE FIX: Removed .name() so it passes the Enum directly
        audit.setStatus(dto.getStatus() != null ? dto.getStatus() : AuditStatus.PLANNED);

        Audit saved = auditRepository.save(audit);
        
        userClient.logAuditAction(officerId, "OFFICIAL_AUDIT_CREATED", "AUDIT_ID_" + saved.getAuditId(), "SUCCESS");
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public AuditResponseDTO updateAuditFindings(Long auditId, String findings, String status) {
        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit record", auditId));

        if (findings != null) audit.setFindings(findings);
        if (status != null) {
            try {
                // THE FIX: Removed .name() so it sets the Enum directly
                audit.setStatus(AuditStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status. Allowed: PLANNED, IN_PROGRESS, UNDER_REVIEW, COMPLETED, CANCELLED");
            }
        }

        Audit updated = auditRepository.save(audit);
        userClient.logAuditAction(SecurityUtils.getCurrentUserId(), "OFFICIAL_AUDIT_UPDATED", "AUDIT_ID_" + auditId, "SUCCESS");
        return mapToDto(updated);
    }

    @Override
    public List<AuditResponseDTO> getAllAudits() {
        return auditRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    public Page<AuditResponseDTO> getAuditsByOfficer(Long officerId, int page, int size) {
        return auditRepository.findByOfficerId(officerId, PageRequest.of(page, size)).map(this::mapToDto);
    }

    private AuditResponseDTO mapToDto(Audit audit) {
        return AuditResponseDTO.builder()
                .auditId(audit.getAuditId())
                .officerId(audit.getOfficerId())
                .scope(audit.getScope())
                .findings(audit.getFindings())
                .date(audit.getDate())
                // THE FIX: audit.getStatus() is already an Enum, no need for String conversions
                .status(audit.getStatus())
                .build();
    }
}