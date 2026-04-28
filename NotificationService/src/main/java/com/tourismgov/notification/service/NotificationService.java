package com.tourismgov.notification.service;

import java.util.List;

import com.tourismgov.notification.dto.NotificationRequestDTO;
import com.tourismgov.notification.dto.NotificationResponseDTO;
import com.tourismgov.notification.enums.NotificationCategory;

public interface NotificationService {

    NotificationResponseDTO create(NotificationRequestDTO request);

    List<NotificationResponseDTO> getAll(Long userId);

    List<NotificationResponseDTO> getUnread(Long userId);

    NotificationResponseDTO markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    List<NotificationResponseDTO> getByCategory(Long userId, NotificationCategory category);

    // Broadcast — userId in request body identifies the sender (role check done internally)
    void sendGlobalNotification(NotificationRequestDTO request);
}