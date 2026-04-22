package com.tourismgov.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long notificationId;
    private Long userId;
    private String userName;
    private Long entityId;
    private String subject;
    private String message;
    private String category;
    private String status;
    private LocalDateTime createdDate;
}
