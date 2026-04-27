package com.tourismgov.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tourismgov.report.dto.NotificationDTO;

import java.util.List;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient {
    @GetMapping("/notifications/unread")
    List<NotificationDTO> getUnreadNotifications(@RequestParam("userId") Long userId);
}
