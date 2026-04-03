package com.creditapp.controller;

import com.creditapp.dto.NotificationDTO;
import com.creditapp.entity.User;
import com.creditapp.exception.BusinessException;
import com.creditapp.service.NotificationService;
import com.creditapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class NotificationPageController {

    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * Parent family notifications management page
     */
    @GetMapping("/parent/family-notifications")
    public String parentNotificationsPage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        List<NotificationDTO> notifications = notificationService.getNotificationsByParent(user.getId());
        model.addAttribute("notifications", notifications);
        
        return "parent/notifications";
    }

    /**
     * Child family notifications page with marquee
     */
    @GetMapping("/child/family-notifications")
    public String childNotificationsPage() {
        return "child/notifications";
    }
}
