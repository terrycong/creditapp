package com.creditapp.util;

import com.creditapp.entity.Child;
import com.creditapp.repository.ChildRepository;
import com.creditapp.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public static Long getCurrentUserId(ChildRepository childRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return getUserIdFromUsername(userDetails.getUsername(), childRepository);
        }
        throw new IllegalStateException("无法获取当前用户信息");
    }

    private static Long getUserIdFromUsername(String username, ChildRepository childRepository) {
        return childRepository.findByUsername(username)
                .map(Child::getId)
                .orElseThrow(() -> new ResourceNotFoundException("child not found"));
    }

    public static Long getChildIdFromUsername(String username, ChildRepository childRepository) {
        // Look up child by username from database
        if ("child".equals(username)) {
            return childRepository.findByUsername(username)
                    .map(Child::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("child not found"));
        }
        throw new IllegalArgumentException("未知用户名: " + username);
    }
}
