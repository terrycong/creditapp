package com.creditapp.controller;

import com.creditapp.dto.ApiResponse;
import com.creditapp.dto.CreateNotificationRequest;
import com.creditapp.dto.NotificationDTO;
import com.creditapp.entity.User;
import com.creditapp.exception.BusinessException;
import com.creditapp.service.NotificationService;
import com.creditapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * Create a new notification (Parent only)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationDTO>> createNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateNotificationRequest request) {
        log.info("Creating notification: title={}", request.getTitle());
        
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        NotificationDTO notification = notificationService.createNotification(request, user.getId());
        return ResponseEntity.ok(ApiResponse.success("通知创建成功", notification));
    }

    /**
     * Get all notifications for the current child
     */
    @GetMapping("/child")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getChildNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        List<NotificationDTO> notifications = notificationService.getNotificationsForChild(user.getId());
        return ResponseEntity.ok(ApiResponse.success("获取通知列表成功", notifications));
    }

    /**
     * Get unread notifications for the current child (for marquee display)
     */
    @GetMapping("/child/unread")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUnreadChildNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        List<NotificationDTO> notifications = notificationService.getUnreadNotificationsForChild(user.getId());
        return ResponseEntity.ok(ApiResponse.success("获取未读通知成功", notifications));
    }

    /**
     * Get all notifications created by the current parent
     */
    @GetMapping("/parent")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getParentNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        List<NotificationDTO> notifications = notificationService.getNotificationsByParent(user.getId());
        return ResponseEntity.ok(ApiResponse.success("获取通知历史成功", notifications));
    }

    /**
     * Mark a notification as read
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long notificationId) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        notificationService.markAsRead(notificationId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("通知已标记为已读", null));
    }

    /**
     * Mark all notifications as read
     */
    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success("所有通知已标记为已读", null));
    }

    /**
     * Deactivate a notification (Parent only)
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long notificationId) {
        notificationService.deactivateNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success("通知已删除", null));
    }

    /**
     * Get a single notification by ID
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationDTO>> getNotification(
            @PathVariable Long notificationId) {
        NotificationDTO notification = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(ApiResponse.success("获取通知详情成功", notification));
    }
}
