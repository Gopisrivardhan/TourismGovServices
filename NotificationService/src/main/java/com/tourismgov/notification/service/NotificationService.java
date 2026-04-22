package com.tourismgov.notification.service;

import java.util.List;

import com.tourismgov.notification.dto.NotificationRequestDTO;
import com.tourismgov.notification.dto.NotificationResponseDTO;
import com.tourismgov.notification.enums.NotificationCategory;

public interface NotificationService {

    /**
     * Creates a new notification for a specific user.
     * Note: Expects userId and userEmail to be provided in the DTO.
     */
    NotificationResponseDTO create(NotificationRequestDTO request);

    /**
     * Retrieves all notifications for a specific user.
     */
    List<NotificationResponseDTO> getAll(Long userId);

    /**
     * Retrieves only UNREAD notifications for the user.
     */
    List<NotificationResponseDTO> getUnread(Long userId);

    /**
     * Updates notification status to READ.
     */
    NotificationResponseDTO markAsRead(Long notificationId, Long userId);
    
    /**
     * Filters notifications by category for a user.
     */
    List<NotificationResponseDTO> getByCategory(Long userId, NotificationCategory category);
    
    /**
     * Broadcasts a notification. 
     * In a microservice, the request should contain the target audience data.
     */
    void sendGlobalNotification(NotificationRequestDTO request);
}