package com.tourismgov.notification.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tourismgov.notification.client.UserClient;
import com.tourismgov.notification.dto.NotificationRequestDTO;
import com.tourismgov.notification.dto.NotificationResponseDTO;
import com.tourismgov.notification.dto.UserDTO;
import com.tourismgov.notification.enums.NotificationCategory;
import com.tourismgov.notification.enums.NotificationStatus;
import com.tourismgov.notification.exception.ResourceNotFoundException;
import com.tourismgov.notification.model.Notification;
import com.tourismgov.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;
    private final EmailService emailService;

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public NotificationResponseDTO create(NotificationRequestDTO request) {
        if (request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipient userId is required.");
        }

        UserDTO user;
        try {
            user = userClient.getUserById(request.getUserId());
        } catch (Exception e) {
            throw new ResourceNotFoundException("User not found with id: " + request.getUserId());
        }

        if (request.getCategory() == null) {
            request.setCategory(NotificationCategory.SYSTEM);
        }

        Notification notification = Notification.builder()
                .userId(user.getUserId())
                .entityId(request.getEntityId() != null ? request.getEntityId() : 0L)
                .subject(request.getSubject())
                .message(request.getMessage())
                .category(request.getCategory())
                .status(NotificationStatus.UNREAD)
                .build();

        Notification saved = notificationRepository.saveAndFlush(notification);
        log.info("Notification created: id={} for userId={}", saved.getNotificationId(), user.getUserId());

        // Email is non-critical — failure does NOT rollback
        try {
            emailService.sendNotificationEmail(user.getEmail(), user.getName(), request.getSubject(), request.getMessage());
        } catch (Exception e) {
            log.warn("Email failed for userId={}: {}", user.getUserId(), e.getMessage());
        }

        return toDTO(saved, user.getName());
    }

    // ─── BROADCAST ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void sendGlobalNotification(NotificationRequestDTO request) {
        // Fetch sender and check role
        UserDTO sender;
        try {
            sender = userClient.getUserById(request.getUserId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender user not found.");
        }

        if ("TOURIST".equals(sender.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tourists cannot send broadcasts.");
        }

        if (request.getCategory() == null) {
            request.setCategory(NotificationCategory.SYSTEM);
        }

        List<UserDTO> allUsers;
        try {
            allUsers = userClient.getAllUsers();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to retrieve user list. Please try again later.");
        }

        if (allUsers == null || allUsers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found to broadcast to.");
        }

        final NotificationCategory category = request.getCategory();
        final Long entityId = request.getEntityId() != null ? request.getEntityId() : 0L;

        List<Notification> batch = allUsers.stream()
                .map(user -> Notification.builder()
                        .userId(user.getUserId())
                        .subject(request.getSubject())
                        .message(request.getMessage())
                        .category(category)
                        .entityId(entityId)
                        .status(NotificationStatus.UNREAD)
                        .build())
                .collect(Collectors.toList());

        notificationRepository.saveAll(batch);
        log.info("Broadcast by userId={} to {} users. Subject='{}'",
                sender.getUserId(), batch.size(), request.getSubject());

        allUsers.forEach(user -> {
            try {
                emailService.sendNotificationEmail(user.getEmail(), user.getName(),
                        request.getSubject(), request.getMessage());
            } catch (Exception e) {
                log.warn("Email failed for userId={}: {}", user.getUserId(), e.getMessage());
            }
        });
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @Override
    public List<NotificationResponseDTO> getAll(Long userId) {
        verifyUserExists(userId);
        return notificationRepository.findByUserIdOrderByCreatedDateDesc(userId)
                .stream().map(n -> toDTO(n, null)).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponseDTO> getUnread(Long userId) {
        verifyUserExists(userId);
        return notificationRepository.findByUserIdAndStatusOrderByCreatedDateDesc(userId, NotificationStatus.UNREAD)
                .stream().map(n -> toDTO(n, null)).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponseDTO> getByCategory(Long userId, NotificationCategory category) {
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category value.");
        }
        verifyUserExists(userId);
        return notificationRepository.findByUserIdAndCategoryOrderByCreatedDateDesc(userId, category)
                .stream().map(n -> toDTO(n, null)).collect(Collectors.toList());
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification #" + notificationId + " not found or does not belong to you."));

        if (notification.getStatus() == NotificationStatus.READ) {
            return toDTO(notification, null); // idempotent
        }

        notification.setStatus(NotificationStatus.READ);
        return toDTO(notificationRepository.save(notification), null);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        verifyUserExists(userId);
        List<Notification> unread = notificationRepository
                .findByUserIdAndStatusOrderByCreatedDateDesc(userId, NotificationStatus.UNREAD);

        if (unread.isEmpty()) return;

        unread.forEach(n -> n.setStatus(NotificationStatus.READ));
        notificationRepository.saveAll(unread);
        log.info("Marked {} notifications as READ for userId={}", unread.size(), userId);
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private void verifyUserExists(Long userId) {
        try {
            UserDTO user = userClient.getUserById(userId);
            if (user == null) throw new ResourceNotFoundException("User not found with id: " + userId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            // If UserService is down, allow reads to proceed (JWT already validated the user at Gateway)
            log.warn("UserService unreachable for userId={}. Proceeding with JWT trust.", userId);
        }
    }

    private NotificationResponseDTO toDTO(Notification n, String name) {
        return NotificationResponseDTO.builder()
                .notificationId(n.getNotificationId())
                .userId(n.getUserId())
                .userName(name)
                .entityId(n.getEntityId())
                .subject(n.getSubject())
                .message(n.getMessage())
                .category(n.getCategory())
                .status(n.getStatus())
                .createdDate(n.getCreatedDate())
                .build();
    }
}