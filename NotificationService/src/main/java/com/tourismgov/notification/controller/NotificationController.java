package com.tourismgov.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.notification.dto.NotificationRequestDTO;
import com.tourismgov.notification.dto.NotificationResponseDTO;
import com.tourismgov.notification.enums.NotificationCategory;
import com.tourismgov.notification.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications") // Standard microservice naming
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 1. Create - Used by other services (like Report) to send a notification
    @PostMapping
    public ResponseEntity<NotificationResponseDTO> create(
            @Valid @RequestBody NotificationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(request));
    }

    // 2. Get All - Uses header injected by API Gateway for security
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getAll(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notificationService.getAll(userId));
    }
    
    // 3. Get by Category - User ID from Header, Category from Path
    @GetMapping("/category/{category}")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByCategory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable NotificationCategory category) {
        return ResponseEntity.ok(notificationService.getByCategory(userId, category));
    }

    // 4. Get Unread - Uses header
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnread(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notificationService.getUnread(userId));
    }

    // 5. Mark as Read - Uses ID from path and UserID from header to verify ownership
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notificationService.markAsRead(id, userId));
    }
    
    // 6. Broadcast - Still requires the full request body
    @PostMapping("/broadcast")
    public ResponseEntity<String> broadcast(@Valid @RequestBody NotificationRequestDTO request) {
        notificationService.sendGlobalNotification(request);
        return ResponseEntity.ok("Global Broadcast Successful!");
    }
    
    /**
     * Internal endpoint for other microservices to send system alerts.
     * Maps RequestParams to the Notification entity logic.
     */
    @PostMapping("/system-alert")
    public ResponseEntity<Void> sendSystemAlert(
            @RequestParam("userId") Long userId,
            @RequestParam("entityId") Long entityId,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam("category") String category) {
        
        // 1. Create the Request DTO internally
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setUserId(userId);
        request.setEntityId(entityId);
        request.setSubject(subject);
        request.setMessage(message);
        
        // 2. Safely convert String to Enum
        try {
            request.setCategory(NotificationCategory.valueOf(category.toUpperCase()));
        } catch (IllegalArgumentException e) {
            request.setCategory(NotificationCategory.SYSTEM); // Fallback
        }

        // 3. Call service to save to database
        notificationService.create(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}