package com.tourismgov.compliance.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.tourismgov.compliance.dto.AuditRequestDTO;
import com.tourismgov.compliance.dto.AuditResponseDTO;

public interface AuditService {
    AuditResponseDTO recordAudit(AuditRequestDTO dto);
    AuditResponseDTO updateAuditFindings(Long auditId, String findings, String status);
    List<AuditResponseDTO> getAllAudits();
    Page<AuditResponseDTO> getAuditsByOfficer(Long officerId, int page, int size);
}