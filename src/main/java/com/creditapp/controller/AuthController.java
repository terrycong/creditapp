package com.creditapp.controller;

import com.creditapp.dto.ApiResponse;
import com.creditapp.entity.User;
import com.creditapp.security.CustomUserDetailsService;
import com.creditapp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ApiResponse<Void> login(@RequestBody LoginRequest request) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            if (userDetails == null) {
                return ApiResponse.error("USER_NOT_FOUND", "用户名不存在");
            }

            if (!userDetails.isEnabled()) {
                return ApiResponse.error("USER_DISABLED", "用户已被禁用");
            }

            return ApiResponse.success("登录成功", null);
        } catch (Exception e) {
            return ApiResponse.error("LOGIN_FAILED", "登录失败：" + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        SecurityContextHolder.clearContext();
        return ApiResponse.success("登出成功", null);
    }

    @GetMapping("/current-user")
    public ApiResponse<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ApiResponse.error("NOT_LOGGED_IN", "未登录");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success("获取用户信息成功", null);
    }
}
