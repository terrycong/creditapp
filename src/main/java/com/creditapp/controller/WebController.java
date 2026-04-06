package com.creditapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web 页面控制器
 */
@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/parent/dashboard")
    public String parentDashboard() {
        return "parent/dashboard";
    }

    @GetMapping("/child/dashboard")
    public String childDashboard() {
        return "child/dashboard";
    }

    // ========== 反馈管理页面 ==========

    /**
     * 孩子反馈页面
     */
    @GetMapping("/child/feedback")
    public String childFeedback() {
        return "feedback/child-feedback";
    }

    /**
     * 家长反馈管理页面
     */
    @GetMapping("/parent/feedback")
    public String parentFeedback() {
        return "feedback/parent-feedback";
    }
}
