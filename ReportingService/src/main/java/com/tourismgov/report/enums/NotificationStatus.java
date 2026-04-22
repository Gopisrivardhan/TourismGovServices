package com.tourismgov.report.enums;

/**
 * NotificationStatus enum - status of a notification.
 *
 * As per document Section 4.8 (Notifications and Alerts):
 *   UNREAD - notification is new, user has not seen it yet
 *   READ   - user has clicked and seen this notification
 *
 * Used in Notification entity and NotificationResponseDTO.
 * Stored as STRING in MySQL using @Enumerated(EnumType.STRING).
 */
public enum NotificationStatus {
    UNREAD,
    READ
}

