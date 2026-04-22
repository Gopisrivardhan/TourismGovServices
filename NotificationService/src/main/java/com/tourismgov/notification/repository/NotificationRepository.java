package com.tourismgov.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tourismgov.notification.enums.NotificationCategory;
import com.tourismgov.notification.enums.NotificationStatus;
import com.tourismgov.notification.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedDateDesc(Long userId);

    List<Notification> findByUserIdAndStatusOrderByCreatedDateDesc(Long userId, NotificationStatus status);

    Optional<Notification> findByNotificationIdAndUserId(Long notificationId, Long userId);

    long countByUserIdAndStatus(Long userId, NotificationStatus status);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status WHERE n.notificationId = :id AND n.userId = :userId")
    int updateStatus(@Param("status") NotificationStatus status, @Param("id") Long id, @Param("userId") Long userId);
    
    List<Notification> findByUserIdAndCategoryOrderByCreatedDateDesc(Long userId, NotificationCategory category);
}