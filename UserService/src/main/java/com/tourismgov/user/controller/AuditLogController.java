package com.tourismgov.user.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.user.dto.AuditLogRequest;
import com.tourismgov.user.dto.AuditLogResponse;
import com.tourismgov.user.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tourismgov/v1/audit-logs") 
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;
    
    @PostMapping
    public ResponseEntity<Void> createAuditLog(@RequestBody AuditLogRequest request) {
        // Make sure your AuditLogService has a method to save this request!
        auditLogService.createLog(request); 
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAll(
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {

        return ResponseEntity.ok(auditLogService.getAllLogs(pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLogResponse>> byUser(
            @PathVariable Long userId,
            Pageable pageable) {

        return ResponseEntity.ok(
            auditLogService.getLogsByUserId(userId, pageable)
        );
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<Page<AuditLogResponse>> byAction(
            @PathVariable String action,
            Pageable pageable) {

        return ResponseEntity.ok(
            auditLogService.getLogsByAction(action, pageable)
        );
    }

    @GetMapping("/dates")
    public ResponseEntity<Page<AuditLogResponse>> byDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end,
            Pageable pageable) {

        return ResponseEntity.ok(
            auditLogService.getLogsByDateRange(start, end, pageable)
        );
    }
}