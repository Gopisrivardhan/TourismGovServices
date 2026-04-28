package com.tourismgov.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tourismgov.report.dto.NotificationDTO;

import java.util.List;

@FeignClient(name = "NOTIFICATIONSERVICE")
public interface NotificationClient {

    @GetMapping("/tourismgov/v1/notifications/internal/unread")
    List<NotificationDTO> getUnreadNotifications(@RequestParam("userId") Long userId);

    @PostMapping("/tourismgov/v1/notifications/system-alert")
    void sendSystemAlert(
            @RequestParam("userId") Long userId,
            @RequestParam("entityId") Long entityId,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam("category") String category);
}
