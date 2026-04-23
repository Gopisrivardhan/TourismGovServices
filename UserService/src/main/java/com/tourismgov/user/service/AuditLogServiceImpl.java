package com.tourismgov.user.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.user.dto.AuditLogRequest;
import com.tourismgov.user.dto.AuditLogResponse;
import com.tourismgov.user.entity.AuditLog;
import com.tourismgov.user.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    
    
    @Override
    @Transactional
    public void createLog(AuditLogRequest request) {
        log.info("Saving audit log -> User ID: {}, Action: {}, Status: {}", 
                 request.getUserId(), request.getAction(), request.getStatus());

        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(request.getUserId());
        auditLog.setAction(request.getAction());
        auditLog.setResource(request.getResource());
        auditLog.setStatus(request.getStatus()); 
        
        // NO NEED to set timestamp here! @CreationTimestamp handles it.

        auditLogRepository.save(auditLog);
    }

    /**
     * Security / system audits – must survive rollback
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(
            Long userId,
            String action,
            String resource,
            String status) {

        if (userId == null) {
            log.warn("Audit skipped – userId is null");
            return;
        }

        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .resource(resource)
                .status(status)
                .build();

        auditLogRepository.save(auditLog);
    }

    /**
     * Business audits – rollback with main transaction
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void logActionInCurrentTransaction(
            Long userId,
            String action,
            String resource,
            String status) {

        if (userId == null) {
            log.warn("Audit skipped – userId is null for action {}", action);
            return;
        }

        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .resource(resource)
                .status(status)
                .build();

        auditLogRepository.save(auditLog);
    }

    @Override
    public Page<AuditLogResponse> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getLogsByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getLogsByDateRange(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable) {

        return auditLogRepository
                .findByTimestampBetween(start, end, pageable)
                .map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .auditId(log.getAuditId())
                .userId(log.getUserId())   // ✅ FIXED
                .action(log.getAction())
                .resource(log.getResource())
                .status(log.getStatus())
                .timestamp(log.getTimestamp())
                .build();
    }
}