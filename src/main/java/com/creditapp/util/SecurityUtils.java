package com.creditapp.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // In real implementation, you would extract user ID from the principal
            // For now, we'll use a simple approach by parsing username
            return getUserIdFromUsername(userDetails.getUsername());
        }
        throw new IllegalStateException("无法获取当前用户信息");
    }

    public static Long getChildIdFromUsername(String username) {
        return getUserIdFromUsername(username);
    }

    private static Long getUserIdFromUsername(String username) {
        // Simple implementation - in real app, you would load user from database
        if ("parent".equals(username)) {
            return 1L;
        } else if ("child".equals(username)) {
            return 2L;
        }
        throw new IllegalArgumentException("未知用户名: " + username);
    }
}