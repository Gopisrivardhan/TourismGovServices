package com.tourismgov.program.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient {

    /**
     * Sends a system alert notification to users or administrators.
     */
    @PostMapping("/tourismgov/v1/notifications/system-alert")
    void sendSystemAlert(
            @RequestParam("userId") Long userId,
            @RequestParam("referenceId") Long referenceId,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam("role") String role
    );
}