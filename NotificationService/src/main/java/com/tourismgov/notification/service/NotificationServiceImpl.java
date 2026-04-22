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
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient; 
    private final EmailService emailService; 

    @Override
    @Transactional
    public NotificationResponseDTO create(NotificationRequestDTO request) {
        // Logic Update: Catch Feign 404 and throw your specific "user is not exist" message
        UserDTO user;
        try {
            user = userClient.getUserById(request.getUserId());
        } catch (Exception e) {
            throw new ResourceNotFoundException("user is not exist");
        }

        Notification notification = Notification.builder()
                .userId(user.getUserId())
                .entityId(request.getEntityId())
                .subject(request.getSubject())
                .message(request.getMessage())
                .category(request.getCategory())
                .status(NotificationStatus.UNREAD)
                .build();

        Notification saved = notificationRepository.saveAndFlush(notification);

        try {
            emailService.sendNotificationEmail(user.getEmail(), user.getName(), request.getSubject(), request.getMessage());
        } catch (Exception e) {
            log.error("EMAIL FAILED for user {}: {}", user.getEmail(), e.getMessage());
        }

        return toDTO(saved, user.getName());
    }

    @Override
    @Transactional
    public void sendGlobalNotification(NotificationRequestDTO request) {
        UserDTO sender = userClient.getUserById(request.getUserId());
        if ("TOURIST".equals(sender.getRole())) { 
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tourists cannot send broadcasts.");
        }

        List<UserDTO> allUsers = userClient.getAllUsers();
        List<Notification> batch = allUsers.stream()
            .map(user -> Notification.builder()
                .userId(user.getUserId())
                .subject(request.getSubject())
                .message(request.getMessage())
                .category(request.getCategory())
                .entityId(request.getEntityId() != null ? request.getEntityId() : 0L)
                .status(NotificationStatus.UNREAD)
                .build())
            .toList();

        notificationRepository.saveAll(batch);
        allUsers.forEach(user -> emailService.sendNotificationEmail(user.getEmail(), user.getName(), request.getSubject(), request.getMessage()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getAll(Long userId) {
        verifyUserExists(userId);
        return notificationRepository.findByUserIdOrderByCreatedDateDesc(userId) 
                .stream().map(n -> toDTO(n, null)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUnread(Long userId) {
        verifyUserExists(userId);
        return notificationRepository.findByUserIdAndStatusOrderByCreatedDateDesc(userId, NotificationStatus.UNREAD)
                .stream().map(n -> toDTO(n, null)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        notification.setStatus(NotificationStatus.READ);
        return toDTO(notificationRepository.save(notification), null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getByCategory(Long userId, NotificationCategory category) {
        verifyUserExists(userId);
        return notificationRepository.findByUserIdAndCategoryOrderByCreatedDateDesc(userId, category)
                .stream().map(n -> toDTO(n, null)).collect(Collectors.toList());
    }

    private void verifyUserExists(Long userId) {
        // Logic Update: Ensure 404s from User-Service return your specific string
        try {
            if (!userClient.existsById(userId)) {
                throw new ResourceNotFoundException("user is not exist");
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("user is not exist");
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