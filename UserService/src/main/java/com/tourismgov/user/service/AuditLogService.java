package com.tourismgov.user.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tourismgov.user.dto.AuditLogRequest;
import com.tourismgov.user.dto.AuditLogResponse;

public interface AuditLogService {

    // Internal audit logging
    void logAction(Long userId, String action, String resource, String status);

    void logActionInCurrentTransaction(Long userId, String action, String resource, String status);

    // Reporting
    Page<AuditLogResponse> getAllLogs(Pageable pageable);

    Page<AuditLogResponse> getLogsByUserId(Long userId, Pageable pageable);

    Page<AuditLogResponse> getLogsByDateRange(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    Page<AuditLogResponse> getLogsByAction(String action, Pageable pageable);

	void createLog(AuditLogRequest request);
}
