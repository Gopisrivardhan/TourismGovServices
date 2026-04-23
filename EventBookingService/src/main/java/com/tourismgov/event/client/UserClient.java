package com.tourismgov.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.tourismgov.event.dto.AuditLogRequest;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {
    @PostMapping("/tourismgov/v1/audit-logs")
    void logAction(@RequestBody AuditLogRequest request);
}