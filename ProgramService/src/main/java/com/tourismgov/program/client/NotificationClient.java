package com.tourismgov.program.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "NOTIFICATIONSERVICE")
public interface NotificationClient {

    /**
     * Sends a system alert notification to users or administrators.
     */
    @PostMapping("/tourismgov/v1/notifications/system-alert")
    void sendSystemAlert(
            @RequestParam("userId") Long userId,
            @RequestParam("entityId") Long entityId,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam("category") String category
    );
}