package com.creditapp.controller;

import com.creditapp.dto.*;
import com.creditapp.entity.TaskType;
import com.creditapp.entity.User;
import com.creditapp.repository.ChildRepository;
import com.creditapp.service.DashboardService;
import com.creditapp.service.RewardService;
import com.creditapp.service.TaskService;
import com.creditapp.service.UserService;
import com.creditapp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private static final Logger log = LoggerFactory.getLogger(ViewController.class);
    private final UserService userService;
    private final DashboardService dashboardService;
    private final TaskService taskService;
    private final RewardService rewardService;
    private final ChildRepository childRepository;

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

    @GetMapping("/parent/tasks")
    public String parentTasks(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User parent = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        // Get tasks created by this parent
        List<TaskDTO> tasks = taskService.getTasksByParent(parent.getId());
        model.addAttribute("tasks", tasks);
        
        // Get children for the task creation form
        List<ChildDTO> children = userService.getChildrenByParentId(parent.getId());
        model.addAttribute("children", children);
        
        return "parent/tasks";
    }

    @PostMapping("/parent/tasks")
    public String createTask(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam String title,
                           @RequestParam String description,
                           @RequestParam Integer points,
                           @RequestParam String type,
                           @RequestParam Long childId,
                           RedirectAttributes redirectAttrs) {
        log.info("Creating task: title={}, points={}, type={}, childId={}", 
                title, points, type, childId);
        
        try {
            // Get current user (parent)
            String username = userDetails.getUsername();
            User parent = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            
            // Create task request
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setPoints(points);
            
            // Convert type string to TaskType enum
            TaskType taskType;
            try {
                taskType = TaskType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                redirectAttrs.addFlashAttribute("error", "无效的任务类型: " + type);
                return "redirect:/parent/tasks";
            }
            request.setType(taskType);
            request.setAssignedChildId(childId);
            
            // Call task service
            taskService.createTask(request, parent.getId());
            
            redirectAttrs.addFlashAttribute("success", "任务创建成功！");
            return "redirect:/parent/tasks?success=true";
        } catch (Exception e) {
            log.error("Failed to create task", e);
            redirectAttrs.addFlashAttribute("error", "创建任务失败: " + e.getMessage());
            return "redirect:/parent/tasks";
        }
    }

    @GetMapping("/parent/rewards")
    public String parentRewards(Model model) {
        // Get all active rewards
        List<RewardDTO> rewards = rewardService.getAllRewards();
        model.addAttribute("rewards", rewards);
        return "parent/rewards";
    }

    @PostMapping("/parent/tasks/{id}/delete")
    public String deleteTask(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable Long id,
                           RedirectAttributes redirectAttrs) {
        log.info("Deleting task: taskId={}", id);
        try {
            // Verify current user is the task creator (optional security check)
            TaskDTO task = taskService.getTaskById(id);
            Long currentUserId = SecurityUtils.getCurrentUserId(childRepository);
            if (!task.getCreatedById().equals(currentUserId)) {
                redirectAttrs.addFlashAttribute("error", "无权删除此任务");
                return "redirect:/parent/tasks";
            }

            taskService.deleteTask(id);
            redirectAttrs.addFlashAttribute("success", "任务删除成功");
        } catch (Exception e) {
            log.error("Failed to delete task", e);
            redirectAttrs.addFlashAttribute("error", "删除任务失败: " + e.getMessage());
        }
        return "redirect:/parent/tasks";
    }

    @PostMapping("/parent/rewards/{id}/delete")
    public String deleteReward(@AuthenticationPrincipal UserDetails userDetails,
                             @PathVariable Long id,
                             RedirectAttributes redirectAttrs) {
        log.info("Deleting reward: rewardId={}", id);
        try {
            rewardService.deleteReward(id);
            redirectAttrs.addFlashAttribute("success", "礼物删除成功");
        } catch (Exception e) {
            log.error("Failed to delete reward", e);
            redirectAttrs.addFlashAttribute("error", "删除礼物失败: " + e.getMessage());
        }
        return "redirect:/parent/rewards";
    }

    @PostMapping("/parent/rewards")
    public String createReward(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam String name,
                             @RequestParam String description,
                             @RequestParam Integer pointsRequired,
                             @RequestParam(required = false) Integer quantity,
                             @RequestParam(required = false) String imageUrl,
                             Model model) {
        log.info("Creating reward: name={}, pointsRequired={}, quantity={}, imageUrl={}", 
                name, pointsRequired, quantity, imageUrl);
        
        try {
            // Create task request (misnamed - should be CreateRewardRequest)
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(name);  // RewardService expects title for name
            request.setDescription(description);
            request.setPoints(pointsRequired);  // RewardService expects points for pointsRequired
            
            // Call reward service
            rewardService.createReward(request);
            
            model.addAttribute("success", "礼物添加成功！");
        } catch (Exception e) {
            log.error("Failed to create reward", e);
            model.addAttribute("error", "添加礼物失败: " + e.getMessage());
        }
        
        // Re-fetch rewards to show the new one
        List<RewardDTO> rewards = rewardService.getAllRewards();
        model.addAttribute("rewards", rewards);
        
        return "parent/rewards";
    }

    // Parent children management page
    @GetMapping("/parent/children")
    public String children(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("=== PARENT CHILDREN CONTROLLER INVOKED ===");
        if (userDetails == null) {
            log.warn("UserDetails is null - user not authenticated!");
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User parent = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

        // Fetch all children for this parent with task counts
        List<ChildDTO> children = userService.getChildrenByParentId(parent.getId());
        model.addAttribute("children", children);

        // Add username for display
        model.addAttribute("username", username);

        log.info("Found {} children for parent: {}", children.size(), username);
        return "parent/children";
    }

    @GetMapping("/child/tasks")
    public String childTasks(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("=== CHILD TASKS CONTROLLER INVOKED ===");
        if (userDetails == null) {
            log.warn("UserDetails is null - user not authenticated!");
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

        log.info("Child tasks accessed by user: {}, userId: {}", username, user.getId());

        // Fetch tasks assigned to this child
        List<TaskDTO> tasks = taskService.getTasksByChild(user.getId());
        model.addAttribute("tasks", tasks);

        // Add username for display
        model.addAttribute("username", username);

        log.info("Found {} tasks for child: {}", tasks.size(), username);
        return "child/tasks";
    }

    @GetMapping("/child/rewards")
    public String childRewards(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("=== CHILD REWARDS CONTROLLER INVOKED ===");
        if (userDetails == null) {
            log.warn("UserDetails is null - user not authenticated!");
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

        log.info("Child rewards accessed by user: {}, userId: {}", username, user.getId());

        // Fetch active rewards for the store
        List<RewardDTO> rewards = rewardService.getAllRewards();
        model.addAttribute("rewards", rewards);

        // Get child's current points
        ChildDTO child = userService.getChildById(user.getId());
        model.addAttribute("childPoints", child.getPoints());

        // Get child's redeemed rewards
        List<RewardRedemptionDTO> redeemedRewards = rewardService.getRedemptionsByChildId(user.getId());
        model.addAttribute("redeemedRewards", redeemedRewards);

        // Add username for display
        model.addAttribute("username", username);

        log.info("Found {} rewards for child: {} with {} points and {} redemptions",
                rewards.size(), username, child.getPoints(), redeemedRewards.size());
        return "child/rewards";
    }

    @GetMapping("/parent/create-child")
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
    
    // Task approvals page for parent
    @GetMapping("/parent/approvals")
    public String parentApprovals(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("=== PARENT APPROVALS CONTROLLER INVOKED ===");
        if (userDetails == null) {
            log.warn("UserDetails is null - user not authenticated!");
            return "redirect:/login";
        }
        
        String username = userDetails.getUsername();
        User parent = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        // Get pending task completions for this parent's children
        List<TaskCompletionDTO> pendingCompletions = taskService.getPendingCompletionsByParent(parent.getId());
        model.addAttribute("pendingCompletions", pendingCompletions);
        
        // Add username for display
        model.addAttribute("username", username);
        
        log.info("Found {} pending completions for parent: {}", pendingCompletions.size(), username);
        return "parent/approvals";
    }
    
    // Approve task completion
    @PostMapping("/parent/approvals/{completionId}/approve")
    public String approveCompletion(@AuthenticationPrincipal UserDetails userDetails,
                                  @PathVariable Long completionId,
                                  RedirectAttributes redirectAttrs) {
        log.info("Approving completion: completionId={}", completionId);
        try {
            taskService.approveCompletion(completionId);
            redirectAttrs.addFlashAttribute("success", "任务已完成，积分已发放！");
            return "redirect:/parent/approvals";
        } catch (Exception e) {
            log.error("Failed to approve completion", e);
            redirectAttrs.addFlashAttribute("error", "审批失败: " + e.getMessage());
            return "redirect:/parent/approvals";
        }
    }
    
    // Reject task completion
    @PostMapping("/parent/approvals/{completionId}/reject")
    public String rejectCompletion(@AuthenticationPrincipal UserDetails userDetails,
                                  @PathVariable Long completionId,
                                  RedirectAttributes redirectAttrs) {
        log.info("Rejecting completion: completionId={}", completionId);
        try {
            taskService.rejectCompletion(completionId);
            redirectAttrs.addFlashAttribute("success", "任务已拒绝");
            return "redirect:/parent/approvals";
        } catch (Exception e) {
            log.error("Failed to reject completion", e);
            redirectAttrs.addFlashAttribute("error", "拒绝失败: " + e.getMessage());
            return "redirect:/parent/approvals";
        }
    }
}
