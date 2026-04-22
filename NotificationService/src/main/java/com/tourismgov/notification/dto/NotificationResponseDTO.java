package com.tourismgov.notification.dto;

import java.time.LocalDateTime;

import com.tourismgov.notification.enums.NotificationCategory;
import com.tourismgov.notification.enums.NotificationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    private Long notificationId;
    private Long userId;
    private String userName; // We will fill this using the UserClient (Feign)
    private String subject;
    private Long entityId;
    private String message;
    private NotificationCategory category;
    private NotificationStatus status;
    private LocalDateTime createdDate;
}