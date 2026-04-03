package com.creditapp.controller;

import com.creditapp.dto.CreateNotificationRequest;
import com.creditapp.entity.Notification;
import com.creditapp.entity.User;
import com.creditapp.entity.UserRole;
import com.creditapp.repository.NotificationRepository;
import com.creditapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User parent;
    private User child;

    @BeforeEach
    void setUp() {
        // Create parent user
        parent = new User();
        parent.setUsername("test_parent");
        parent.setPassword("$2a$10$BFeE1qUtBvthU1sKwEc.tOIzuFOw1D635dDscfC2cv1HcP1/Co0Su");
        parent.setRole(UserRole.PARENT);
        parent.setPoints(0);
        parent = userRepository.save(parent);

        // Create child user
        child = new User();
        child.setUsername("test_child");
        child.setPassword("$2a$10$cV03s.di3hDvqXLPMiAvpucc7aLcLrWv5kHMFWIrOZXWdAtT4SsDi");
        child.setRole(UserRole.CHILD);
        child.setPoints(0);
        child = userRepository.save(child);
    }

    @Test
    @WithMockUser(username = "test_parent", roles = "PARENT")
    void testCreateNotification() throws Exception {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setTitle("Test Notification");
        request.setContent("This is a test notification");
        request.setTargetType("ALL");
        request.setPriority("NORMAL");

        mockMvc.perform(post("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.title").value("Test Notification"));
    }

    @Test
    @WithMockUser(username = "test_child", roles = "CHILD")
    void testGetChildNotifications() throws Exception {
        // Create a notification first
        Notification notification = new Notification();
        notification.setTitle("Test");
        notification.setContent("Content");
        notification.setCreatedById(parent.getId());
        notification.setTargetType(Notification.TargetType.ALL);
        notification.setPriority(Notification.Priority.NORMAL);
        notification.setActive(true);
        notificationRepository.save(notification);

        mockMvc.perform(get("/api/v1/notifications/child"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "test_child", roles = "CHILD")
    void testGetUnreadNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/child/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "test_parent", roles = "PARENT")
    void testGetParentNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/parent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "test_child", roles = "CHILD")
    void testMarkAsRead() throws Exception {
        // Create a notification first
        Notification notification = new Notification();
        notification.setTitle("Test");
        notification.setContent("Content");
        notification.setCreatedById(parent.getId());
        notification.setTargetType(Notification.TargetType.ALL);
        notification.setPriority(Notification.Priority.NORMAL);
        notification.setActive(true);
        notification = notificationRepository.save(notification);

        mockMvc.perform(post("/api/v1/notifications/" + notification.getId() + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "test_child", roles = "CHILD")
    void testMarkAllAsRead() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "test_parent", roles = "PARENT")
    void testDeleteNotification() throws Exception {
        Notification notification = new Notification();
        notification.setTitle("Test");
        notification.setContent("Content");
        notification.setCreatedById(parent.getId());
        notification.setTargetType(Notification.TargetType.ALL);
        notification.setPriority(Notification.Priority.NORMAL);
        notification.setActive(true);
        notification = notificationRepository.save(notification);

        mockMvc.perform(delete("/api/v1/notifications/" + notification.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }
}
