package com.creditapp.service;

import com.creditapp.dto.CreateNotificationRequest;
import com.creditapp.dto.NotificationDTO;
import com.creditapp.entity.Notification;
import com.creditapp.entity.NotificationRead;
import com.creditapp.entity.User;
import com.creditapp.entity.UserRole;
import com.creditapp.repository.NotificationReadRepository;
import com.creditapp.repository.NotificationRepository;
import com.creditapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationReadRepository notificationReadRepository;

    @Autowired
    private UserRepository userRepository;

    private User parent;
    private User child;

    @BeforeEach
    void setUp() {
        parent = new User();
        parent.setUsername("test_parent");
        parent.setPassword("password");
        parent.setRole(UserRole.PARENT);
        parent.setPoints(0);
        parent = userRepository.save(parent);

        child = new User();
        child.setUsername("test_child");
        child.setPassword("password");
        child.setRole(UserRole.CHILD);
        child.setPoints(0);
        child = userRepository.save(child);
    }

    @Test
    void testCreateNotification() {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle("Test Notification");
        request.setContent("Test Content");
        request.setTargetType("ALL");
        request.setPriority("HIGH");

        NotificationDTO result = notificationService.createNotification(request, parent.getId());

        assertNotNull(result);
        assertEquals("Test Notification", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals("HIGH", result.getPriority());
    }

    @Test
    void testGetNotificationsForChild() {
        // Create notifications
        CreateNotificationRequest request1 = new CreateNotificationRequest();
        request1.setTitle("Notification 1");
        request1.setContent("Content 1");
        request1.setTargetType("ALL");
        notificationService.createNotification(request1, parent.getId());

        CreateNotificationRequest request2 = new CreateNotificationRequest();
        request2.setTitle("Notification 2");
        request2.setContent("Content 2");
        request2.setTargetType("ALL");
        notificationService.createNotification(request2, parent.getId());

        List<NotificationDTO> notifications = notificationService.getNotificationsForChild(child.getId());

        assertEquals(2, notifications.size());
    }

    @Test
    void testMarkAsRead() {
        // Create a notification
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle("Test");
        request.setContent("Content");
        request.setTargetType("ALL");
        NotificationDTO notification = notificationService.createNotification(request, parent.getId());

        // Mark as read
        notificationService.markAsRead(notification.getId(), child.getId());

        // Verify it's marked as read
        List<NotificationDTO> unread = notificationService.getUnreadNotificationsForChild(child.getId());
        assertTrue(unread.stream().noneMatch(n -> n.getId().equals(notification.getId())));
    }

    @Test
    void testMarkAllAsRead() {
        // Create multiple notifications
        for (int i = 0; i < 3; i++) {
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setTitle("Notification " + i);
            request.setContent("Content " + i);
            request.setTargetType("ALL");
            notificationService.createNotification(request, parent.getId());
        }

        // Mark all as read
        notificationService.markAllAsRead(child.getId());

        // Verify all are read
        List<NotificationDTO> unread = notificationService.getUnreadNotificationsForChild(child.getId());
        assertEquals(0, unread.size());
    }

    @Test
    void testDeactivateNotification() {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle("Test");
        request.setContent("Content");
        request.setTargetType("ALL");
        NotificationDTO notification = notificationService.createNotification(request, parent.getId());

        notificationService.deactivateNotification(notification.getId());

        NotificationDTO deactivated = notificationService.getNotificationById(notification.getId());
        assertFalse(deactivated.getActive());
    }

    @Test
    void testGetUnreadNotificationsForChild() {
        // Create notifications
        CreateNotificationRequest request1 = new CreateNotificationRequest();
        request1.setTitle("Unread Notification");
        request1.setContent("Content");
        request1.setTargetType("ALL");
        NotificationDTO unread = notificationService.createNotification(request1, parent.getId());

        CreateNotificationRequest request2 = new CreateNotificationRequest();
        request2.setTitle("Read Notification");
        request2.setContent("Content");
        request2.setTargetType("ALL");
        NotificationDTO toMarkRead = notificationService.createNotification(request2, parent.getId());

        // Mark one as read
        notificationService.markAsRead(toMarkRead.getId(), child.getId());

        List<NotificationDTO> unreadList = notificationService.getUnreadNotificationsForChild(child.getId());

        assertEquals(1, unreadList.size());
        assertEquals("Unread Notification", unreadList.get(0).getTitle());
    }
}
