package com.creditapp.service.impl;

import com.creditapp.dto.CreateNotificationRequest;
import com.creditapp.dto.NotificationDTO;
import com.creditapp.entity.Notification;
import com.creditapp.entity.NotificationRead;
import com.creditapp.entity.User;
import com.creditapp.exception.BusinessException;
import com.creditapp.repository.NotificationReadRepository;
import com.creditapp.repository.NotificationRepository;
import com.creditapp.service.NotificationService;
import com.creditapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final UserService userService;

    @Override
    @Transactional
    public NotificationDTO createNotification(CreateNotificationRequest request, Long createdById) {
        log.info("Creating notification: title={}, targetType={}", request.getTitle(), request.getTargetType());

        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setCreatedById(createdById);
        notification.setTargetType(Notification.TargetType.valueOf(request.getTargetType()));
        notification.setTargetChildId(request.getTargetChildId());
        notification.setPriority(request.getPriority() != null ? 
            Notification.Priority.valueOf(request.getPriority()) : Notification.Priority.NORMAL);
        notification.setActive(true);

        notification = notificationRepository.save(notification);
        return convertToDTO(notification, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsForChild(Long childId) {
        log.info("Getting all notifications for child: {}", childId);
        
        List<Long> readIds = notificationReadRepository.findReadNotificationIdsByChildId(childId);
        List<Notification> notifications = notificationRepository.findAllForChild(childId);
        
        return notifications.stream()
            .map(n -> convertToDTO(n, readIds.contains(n.getId())))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotificationsForChild(Long childId) {
        log.info("Getting unread notifications for child: {}", childId);
        
        List<Long> readIds = notificationReadRepository.findReadNotificationIdsByChildId(childId);
        List<Notification> notifications = notificationRepository.findAllForChild(childId);
        
        return notifications.stream()
            .filter(n -> !readIds.contains(n.getId()))
            .map(n -> convertToDTO(n, false))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByParent(Long parentId) {
        log.info("Getting notifications created by parent: {}", parentId);
        
        List<Notification> notifications = notificationRepository.findByCreatedByIdOrderByCreatedAtDesc(parentId);
        return notifications.stream()
            .map(n -> convertToDTO(n, null))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long childId) {
        log.info("Marking notification as read: notificationId={}, childId={}", notificationId, childId);
        
        notificationReadRepository.findByNotificationIdAndChildId(notificationId, childId)
            .orElseGet(() -> {
                NotificationRead read = new NotificationRead();
                read.setNotificationId(notificationId);
                read.setChildId(childId);
                return notificationReadRepository.save(read);
            });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long childId) {
        log.info("Marking all notifications as read for child: {}", childId);
        
        List<Notification> notifications = notificationRepository.findAllForChild(childId);
        for (Notification notification : notifications) {
            markAsRead(notification.getId(), childId);
        }
    }

    @Override
    @Transactional
    public void deactivateNotification(Long notificationId) {
        log.info("Deactivating notification: {}", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new BusinessException("NOTIFICATION_NOT_FOUND", "通知不存在"));
        notification.setActive(false);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new BusinessException("NOTIFICATION_NOT_FOUND", "通知不存在"));
        return convertToDTO(notification, null);
    }

    private NotificationDTO convertToDTO(Notification notification, Boolean read) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setCreatedById(notification.getCreatedById());
        dto.setTargetType(notification.getTargetType().name());
        dto.setTargetChildId(notification.getTargetChildId());
        dto.setPriority(notification.getPriority().name());
        dto.setActive(notification.getActive());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setRead(read);

        // Get creator username
        try {
            User creator = userService.findById(notification.getCreatedById());
            dto.setCreatedByUsername(creator.getUsername());
        } catch (Exception e) {
            dto.setCreatedByUsername("Unknown");
        }

        return dto;
    }
}
