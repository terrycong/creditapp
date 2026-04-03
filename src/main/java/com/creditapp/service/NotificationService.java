package com.creditapp.service;

import com.creditapp.dto.CreateNotificationRequest;
import com.creditapp.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {

    /**
     * Create a new notification
     */
    NotificationDTO createNotification(CreateNotificationRequest request, Long createdById);

    /**
     * Get all notifications for a specific child (with read status)
     */
    List<NotificationDTO> getNotificationsForChild(Long childId);

    /**
     * Get unread notifications for a specific child
     */
    List<NotificationDTO> getUnreadNotificationsForChild(Long childId);

    /**
     * Get all notifications created by a parent
     */
    List<NotificationDTO> getNotificationsByParent(Long parentId);

    /**
     * Mark a notification as read by a child
     */
    void markAsRead(Long notificationId, Long childId);

    /**
     * Mark all notifications as read for a child
     */
    void markAllAsRead(Long childId);

    /**
     * Deactivate a notification
     */
    void deactivateNotification(Long notificationId);

    /**
     * Get a single notification by ID
     */
    NotificationDTO getNotificationById(Long notificationId);
}
