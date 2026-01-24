package com.creditapp.controller;

import com.creditapp.dto.ChildDTO;
import com.creditapp.entity.User;
import com.creditapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private static final Logger log = LoggerFactory.getLogger(ViewController.class);
    private final UserService userService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("=== DASHBOARD CONTROLLER INVOKED ===");
        if (userDetails == null) {
            log.warn("UserDetails is null - user not authenticated!");
            return "redirect:/login";
        }
        
        log.info("Dashboard accessed by user: {}, authorities: {}", 
                userDetails.getUsername(), userDetails.getAuthorities());
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        // Strip "ROLE_" prefix if present
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        log.info("Role after stripping prefix: {}", role);
        model.addAttribute("role", role);
        log.info("=== DASHBOARD RENDERING ===");
        return "dashboard";
    }

    @GetMapping("/parent/children")
    public String parentChildren(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User parent = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        List<ChildDTO> children = userService.getChildrenByParentId(parent.getId());
        model.addAttribute("children", children);
        return "parent/children";
    }

    @GetMapping("/parent/tasks")
    public String parentTasks(Model model) {
        return "parent/tasks";
    }

    @GetMapping("/parent/rewards")
    public String parentRewards(Model model) {
        return "parent/rewards";
    }

    @GetMapping("/child/tasks")
    public String childTasks(Model model) {
        return "child/tasks";
    }

    @GetMapping("/child/rewards")
    public String childRewards(Model model) {
        return "child/rewards";
    }

    @GetMapping("/child/create-child")
    public String showCreateChild() {
        return "common/create-child";
    }

    @PostMapping("/parent/create-child")
    public String createChild(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam String username,
                              @RequestParam String password,
                              Model model) {
        try {
            String parentUsername = userDetails.getUsername();
            User parent = userService.findByUsername(parentUsername)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + parentUsername));
            userService.createChild(parent.getId(), username, password);
            return "redirect:/parent/children?success=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "common/create-child";
        }
    }
}
