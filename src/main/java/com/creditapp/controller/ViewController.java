package com.creditapp.controller;

import com.creditapp.dto.*;
import com.creditapp.entity.User;
import com.creditapp.service.DashboardService;
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
    private final DashboardService dashboardService;

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
        
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        // Strip "ROLE_" prefix if present
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        log.info("Role after stripping prefix: {}", role);
        
        // Get dashboard statistics based on user role
        DashboardStatsDTO dashboardStats;
        if ("PARENT".equals(role)) {
            dashboardStats = dashboardService.getParentDashboardStats(user.getId());
        } else {
            dashboardStats = dashboardService.getChildDashboardStats(user.getId());
        }
        
        model.addAttribute("role", role);
        model.addAttribute("dashboardStats", dashboardStats);
        model.addAttribute("username", username);
        
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

    @PostMapping("/parent/tasks")
    public String createTask(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam String title,
                           @RequestParam String description,
                           @RequestParam Integer points,
                           @RequestParam String type,
                           @RequestParam Long childId,
                           Model model) {
        log.info("Creating task: title={}, points={}, type={}, childId={}", 
                title, points, type, childId);
        // TODO: Implement task creation logic
        model.addAttribute("success", "任务创建成功！");
        return "parent/tasks";
    }

    @GetMapping("/parent/rewards")
    public String parentRewards(Model model) {
        return "parent/rewards";
    }

    @PostMapping("/parent/rewards")
    public String createReward(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam String name,
                             @RequestParam String description,
                             @RequestParam Integer pointsRequired,
                             @RequestParam(required = false) Integer quantity,
                             @RequestParam(required = false) String imageUrl,
                             Model model) {
        log.info("Creating reward: name={}, pointsRequired={}, quantity={}", 
                name, pointsRequired, quantity);
        // TODO: Implement reward creation logic
        model.addAttribute("success", "礼物添加成功！");
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

    // Child details view
    @GetMapping("/parent/children/{childId}")
    public String viewChildDetails(@AuthenticationPrincipal UserDetails userDetails,
                                   @PathVariable Long childId,
                                   Model model) {
        try {
            ChildDetailsDTO childDetails = userService.getChildDetails(childId);
            model.addAttribute("child", childDetails);
            return "parent/child-details";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/parent/children?error=" + e.getMessage();
        }
    }

    // Edit child form
    @GetMapping("/parent/children/{childId}/edit")
    public String editChildForm(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long childId,
                                Model model) {
        try {
            ChildDTO child = userService.getChildById(childId);
            model.addAttribute("child", child);
            return "parent/edit-child";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/parent/children?error=" + e.getMessage();
        }
    }

    // Update child
    @PostMapping("/parent/children/{childId}/update")
    public String updateChild(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long childId,
                              @RequestParam(required = false) String username,
                              @RequestParam(required = false) String password,
                              @RequestParam(required = false) Integer points,
                              Model model) {
        try {
            UpdateChildRequest request = new UpdateChildRequest();
            request.setUsername(username);
            request.setPassword(password);
            request.setPoints(points);
            
            userService.updateChild(childId, request);
            return "redirect:/parent/children?success=更新成功";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/parent/children/" + childId + "/edit?error=" + e.getMessage();
        }
    }

    // Delete child
    @PostMapping("/parent/children/{childId}/delete")
    public String deleteChild(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long childId,
                              Model model) {
        try {
            userService.deleteChild(childId);
            return "redirect:/parent/children?success=删除成功";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/parent/children?error=" + e.getMessage();
        }
    }
}
