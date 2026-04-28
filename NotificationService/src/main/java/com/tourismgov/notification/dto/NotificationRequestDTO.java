package com.tourismgov.notification.dto;

import com.tourismgov.notification.enums.NotificationCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating a notification.
 * <p>
 * When used from the API endpoint POST /notifications:
 *   - {@code userId} = the RECIPIENT's userId (required — who receives this notification)
 *   - The SENDER is identified via the JWT token (X-User-Id header from Gateway)
 * <p>
 * When used internally by other services via system-alert:
 *   - All fields are set programmatically.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDTO {

    /**
     * The ID of the user who will RECEIVE this notification.
     * Required for direct targeted notifications.
     * For broadcasts, leave null — all users are resolved server-side.
     */
    private Long userId;

    /**
     * Optional reference ID to a related entity (e.g., reportId, siteId, programId).
     */
    private Long entityId;

    /**
     * Short subject/title of the notification.
     */
    @NotBlank(message = "Notification subject is required.")
    @Size(max = 100, message = "Subject must not exceed 100 characters.")
    private String subject;

    /**
     * Full notification message body.
     */
    @NotBlank(message = "Notification message cannot be blank.")
    @Size(min = 5, max = 500, message = "Message must be between 5 and 500 characters.")
    private String message;

    /**
     * Notification category. Defaults to SYSTEM if not provided.
     */
    private NotificationCategory category;
}