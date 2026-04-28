package com.tourismgov.site.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.tourismgov.site.dto.NotificationRequestDTO;

@FeignClient(name = "NOTIFICATIONSERVICE")
public interface NotificationClient {

    @PostMapping("/tourismgov/v1/notifications/system-alert")
    void sendSystemAlert(@RequestParam("userId") Long userId,
                         @RequestParam("entityId") Long entityId,
                         @RequestParam("subject") String subject,
                         @RequestParam("message") String message,
                         @RequestParam("category") String category);

    @PostMapping("/tourismgov/v1/notifications/broadcast")
    void sendGlobalBroadcast(@RequestBody NotificationRequestDTO request);
}