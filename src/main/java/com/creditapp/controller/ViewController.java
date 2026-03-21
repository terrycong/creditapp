package com.creditapp.controller;

import com.creditapp.dto.*;
import com.creditapp.entity.Child;
import com.creditapp.entity.PenaltyNotification;
import com.creditapp.entity.PointHistory;
import com.creditapp.entity.PointWallet;
import com.creditapp.entity.TaskCompletion;
import com.creditapp.entity.TaskDeadlineType;
import com.creditapp.entity.TaskType;
import com.creditapp.entity.User;
import com.creditapp.exception.BusinessException;
import com.creditapp.repository.ChildRepository;
import com.creditapp.repository.TaskCompletionRepository;
import com.creditapp.service.DashboardService;
import com.creditapp.service.LotteryService;
import com.creditapp.service.PointWalletService;
import com.creditapp.service.PointHistoryService;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private static final Logger log = LoggerFactory.getLogger(ViewController.class);
    private final UserService userService;
    private final DashboardService dashboardService;
    private final TaskService taskService;
    private final RewardService rewardService;
    private final PointHistoryService pointHistoryService;
    private final ChildRepository childRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final LotteryService lotteryService;
    private final PointWalletService pointWalletService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttrs,
                               Model model) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            // Register the user
            User user = userService.register(request);
            
            // Add success message
            redirectAttrs.addFlashAttribute("success", "注册成功！请使用新账号登录。");
            log.info("User registered successfully: username={}", user.getUsername());
            
            return "redirect:/login";
        } catch (BusinessException e) {
            // Add error message to model
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("error", "注册失败：" + e.getMessage());
            return "register";
        }
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

    // Parent marketplace view
    @GetMapping("/parent/marketplace")
    public String parentMarketplace(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("=== PARENT MARKETPLACE CONTROLLER INVOKED ===");
        if (userDetails == null) {
            log.warn("UserDetails is null - user not authenticated!");
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User parent = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

        log.info("Parent marketplace accessed by user: {}, parentId: {}", username, parent.getId());

        // Get all marketplace tasks (available + picked)
        List<TaskDTO> marketplaceTasks = taskService.getMarketplaceTasksByParent(parent.getId());
        
        // Calculate statistics - available tasks have no active TaskJob
        int totalTasks = marketplaceTasks.size();
        int availableTasks = (int) marketplaceTasks.stream().filter(t -> t.isActive()).count();
        int pickedTasks = totalTasks - availableTasks;
        
        // Get children for displaying who picked tasks
        List<ChildDTO> children = userService.getChildrenByParentId(parent.getId());

        model.addAttribute("marketplaceTasks", marketplaceTasks);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("availableTasks", availableTasks);
        model.addAttribute("pickedTasks", pickedTasks);
        model.addAttribute("children", children);
        model.addAttribute("username", username);

        log.info("Found {} total, {} available, {} picked marketplace tasks for parent: {}",
                totalTasks, availableTasks, pickedTasks, username);
        return "parent/marketplace";
    }

    @PostMapping("/parent/tasks")
    public String createTask(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam String title,
                            @RequestParam String description,
                            @RequestParam Integer points,
                            @RequestParam String type,
                            @RequestParam(required = false) Long childId,
                            @RequestParam(required = false, defaultValue = "false") Boolean marketplace,
                            @RequestParam(required = false, defaultValue = "DAILY") String deadlineType,
                            @RequestParam(required = false, defaultValue = "5") Integer penaltyPoints,
                            RedirectAttributes redirectAttrs) {
        log.info("Creating task: title={}, points={}, type={}, childId={}, marketplace={}",
                title, points, type, childId, marketplace);

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

            // Handle marketplace task: if marketplace is true or childId is null/empty, create as marketplace task
            if (Boolean.TRUE.equals(marketplace) || childId == null) {
                request.setAssignedChildId(null); // Marketplace task has no assigned child
                log.info("Creating marketplace task: assignedChildId=null");
            } else {
                request.setAssignedChildId(childId);
                log.info("Creating assigned task: assignedChildId={}", childId);
            }
            
            // Set mandatory task fields if type is MANDATORY
            if (taskType == TaskType.MANDATORY) {
                request.setDeadlineType(TaskDeadlineType.valueOf(deadlineType.toUpperCase()));
                request.setDeadlineValue(1); // Daily means 1 time per day
                request.setPenaltyPoints(penaltyPoints);
            }
            
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

    @PostMapping("/parent/tasks/{id}/delete")
    public String deleteTask(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable Long id,
                           RedirectAttributes redirectAttrs) {
        log.info("Deleting task: taskId={}", id);
        try {
            // Verify current user is the task creator (optional security check)
            TaskDTO task = taskService.getTaskById(id);
            String username = userDetails.getUsername();
            User parent = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            Long currentUserId = parent.getId();
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

    // Edit task form
    @GetMapping("/parent/tasks/{id}/edit")
    public String editTaskForm(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id,
                               Model model) {
        try {
            TaskDTO task = taskService.getTaskById(id);
            model.addAttribute("task", task);

            // Get children for the dropdown
            String username = userDetails.getUsername();
            User parent = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            List<ChildDTO> children = userService.getChildrenByParentId(parent.getId());
            model.addAttribute("children", children);

            return "parent/edit-task";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/parent/tasks?error=" + e.getMessage();
        }
    }

    // Update task
    @PostMapping("/parent/tasks/{id}/update")
    public String updateTask(@AuthenticationPrincipal UserDetails userDetails,
                             @PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam Integer points,
                             @RequestParam String type,
                             @RequestParam Long childId,
                             RedirectAttributes redirectAttrs) {
        log.info("Updating task: id={}, title={}, points={}, type={}, childId={}",
                id, title, points, type, childId);

        try {
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setPoints(points);
            request.setType(TaskType.valueOf(type.toUpperCase()));
            request.setAssignedChildId(childId);

            taskService.updateTask(id, request);
            redirectAttrs.addFlashAttribute("success", "任务更新成功！");
            return "redirect:/parent/tasks";
        } catch (Exception e) {
            log.error("Failed to update task", e);
            redirectAttrs.addFlashAttribute("error", "更新任务失败: " + e.getMessage());
            return "redirect:/parent/tasks/" + id + "/edit?error=" + e.getMessage();
        }
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

        // Fetch approved task completions for this child
        DashboardStatsDTO dashboardStats = dashboardService.getChildDashboardStats(user.getId());
        model.addAttribute("completedTasks", dashboardStats.getRecentTaskCompletions());

        // Fetch PENDING task completions (submitted but not approved yet)
        List<TaskCompletionDTO> pendingCompletions = taskCompletionRepository
                .findPendingCompletionsByChildId(user.getId())
                .stream()
                .map(tc -> TaskCompletionDTO.builder()
                        .id(tc.getId())
                        .taskId(tc.getTask().getId())
                        .taskTitle(tc.getTask().getTitle())
                        .taskPoints(tc.getTask().getPoints())
                        .childId(tc.getChild().getId())
                        .childName(tc.getChild().getUsername())
                        .status(tc.getStatus())
                        .completedAt(tc.getCompletedAt())
                        .build())
                .collect(Collectors.toList());
        model.addAttribute("pendingTasks", pendingCompletions);

        // Get child's current points
        ChildDTO child = userService.getChildById(user.getId());
        model.addAttribute("childPoints", child.getPoints());

        // Get children list for task creation form (including the current child)
        List<ChildDTO> children = userService.getChildrenByParentId(child.getParentId());
        model.addAttribute("children", children);

        // Add username for display
        model.addAttribute("username", username);

        log.info("Found {} tasks, {} completed, {} pending for child: {}",
                tasks.size(), dashboardStats.getRecentTaskCompletions().size(),
                pendingCompletions.size(), username);
        return "child/tasks";
    }

    // Withdraw (delete) a task completion request
    @PostMapping("/child/completions/{id}/withdraw")
    public String withdrawCompletion(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable Long id,
                                     RedirectAttributes redirectAttrs) {
        log.info("Child withdrawing completion request: completionId={}", id);
        try {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            // Verify the completion belongs to this child and is in PENDING status
            taskService.withdrawCompletion(id, user.getId());

            redirectAttrs.addFlashAttribute("success", "任务完成申请已成功撤回！");
            return "redirect:/child/tasks";
        } catch (Exception e) {
            log.error("Failed to withdraw completion", e);
            redirectAttrs.addFlashAttribute("error", "撤回失败: " + e.getMessage());
            return "redirect:/child/tasks";
        }
    }

    // Child creates a draft task
    @PostMapping("/child/tasks")
    public String createDraftTask(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam String title,
                                  @RequestParam String description,
                                  @RequestParam Integer points,
                                  @RequestParam String type,
                                  @RequestParam Long childId,
                                  RedirectAttributes redirectAttrs) {
        log.info("Child creating draft task: title={}, points={}, type={}, childId={}",
                title, points, type, childId);

        try {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setPoints(points);
            request.setType(TaskType.valueOf(type.toUpperCase()));
            request.setAssignedChildId(childId);

            taskService.createDraftTask(request, user.getId());
            redirectAttrs.addFlashAttribute("success", "草稿任务已创建，等待家长审批！");
            return "redirect:/child/tasks";
        } catch (Exception e) {
            log.error("Failed to create draft task", e);
            redirectAttrs.addFlashAttribute("error", "创建草稿任务失败: " + e.getMessage());
            return "redirect:/child/tasks";
        }
    }

    // Get my draft tasks (for child)
    @GetMapping("/child/tasks/drafts")
    public String myDraftTasks(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

        List<TaskDTO> draftTasks = taskService.getDraftTasksByChild(user.getId());
        model.addAttribute("draftTasks", draftTasks);
        model.addAttribute("username", username);

        return "child/drafts";
    }

    // Withdraw (delete) a draft task
    @PostMapping("/child/drafts/{id}/withdraw")
    public String withdrawDraftTask(@AuthenticationPrincipal UserDetails userDetails,
                                    @PathVariable Long id,
                                    RedirectAttributes redirectAttrs) {
        log.info("Child withdrawing draft task: taskId={}", id);
        try {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            // Verify the draft task belongs to this child
            TaskDTO task = taskService.getTaskById(id);
            if (!task.getCreatedById().equals(user.getId())) {
                throw new BusinessException("PERMISSION_DENIED", "无权撤回此草稿任务");
            }

            taskService.deleteTask(id);
            redirectAttrs.addFlashAttribute("success", "草稿任务已成功撤回删除！");
            return "redirect:/child/tasks/drafts";
        } catch (Exception e) {
            log.error("Failed to withdraw draft task", e);
            redirectAttrs.addFlashAttribute("error", "撤回失败: " + e.getMessage());
            return "redirect:/child/tasks/drafts";
        }
    }

    // ========== Point History ==========

    // Get point history for child
    @GetMapping("/child/points/history")
    public String pointHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("=== CHILD POINT HISTORY CONTROLLER INVOKED ===");
        if (userDetails == null) {
            log.warn("UserDetails is null - user not authenticated!");
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

        log.info("Point history accessed by user: {}, userId: {}", username, user.getId());

        // Fetch point history for this child
        List<PointHistoryDTO> pointHistory = pointHistoryService.getPointHistoryByChildId(user.getId());
        model.addAttribute("pointHistory", pointHistory);
        model.addAttribute("username", username);
        model.addAttribute("childId", user.getId());

        return "child/points-history";
    }

    // ========== Point Wallet ==========

    @GetMapping("/child/wallet")
    public String wallet(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在：" + username));

        log.info("Wallet page accessed by user: {}, userId: {}", username, user.getId());

        // Get child entity
        Child child = childRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Child not found: " + user.getId()));

        // Get wallet statistics
        int totalPoints = pointWalletService.getTotalPoints(child);
        int expiredPoints = pointWalletService.getTotalExpiredPoints(child);

        // Get all point batches and enrich with expiration info
        List<PointWallet> allBatches = pointWalletService.getAllPointBatches(child);
        List<PointWalletDTO> walletDTOs = allBatches.stream()
                .map(wallet -> {
                    PointWalletDTO dto = toWalletDTO(wallet);
                    // Calculate days until expiration
                    if (wallet.getExpirationDate() != null && !wallet.isExpired()) {
                        long daysUntilExpiration = ChronoUnit.DAYS.between(
                                LocalDate.now(), 
                                wallet.getExpirationDate());
                        dto.setDaysUntilExpiration((int) daysUntilExpiration);
                        dto.setExpiringSoon(daysUntilExpiration <= 7);
                    }
                    return dto;
                })
                .sorted((a, b) -> {
                    // Sort by expiration date (expiring soon first), then by earned date
                    if (a.getExpirationDate() == null) return 1;
                    if (b.getExpirationDate() == null) return -1;
                    return a.getExpirationDate().compareTo(b.getExpirationDate());
                })
                .collect(Collectors.toList());

        // Calculate expiring soon points (within 7 days)
        int expiringSoonPoints = walletDTOs.stream()
                .filter(dto -> dto.getDaysUntilExpiration() != null && 
                               dto.getDaysUntilExpiration() > 0 && 
                               dto.getDaysUntilExpiration() <= 7 &&
                               !dto.getExpired() &&
                               dto.getRemainingPoints() > 0)
                .mapToInt(PointWalletDTO::getRemainingPoints)
                .sum();

        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("expiringSoonPoints", expiringSoonPoints);
        model.addAttribute("expiredPoints", expiredPoints);
        model.addAttribute("walletBatches", walletDTOs);
        model.addAttribute("username", username);

        return "child/wallet";
    }

    // Helper method to convert PointWallet to DTO
    private PointWalletDTO toWalletDTO(PointWallet wallet) {
        PointWalletDTO dto = new PointWalletDTO();
        dto.setId(wallet.getId());
        dto.setChildId(wallet.getChild().getId());
        dto.setOriginalPoints(wallet.getOriginalPoints());
        dto.setRemainingPoints(wallet.getRemainingPoints());
        dto.setEarnedDate(wallet.getEarnedDate());
        dto.setExpirationDate(wallet.getExpirationDate());
        dto.setSourceType(wallet.getSourceType());
        dto.setSourceId(wallet.getSourceId());
        dto.setFullySpent(wallet.isFullySpent());
        dto.setExpired(wallet.isExpired());
        dto.setExpiredDate(wallet.getExpiredDate());
        dto.setCreatedAt(wallet.getCreatedAt());
        dto.setUpdatedAt(wallet.getUpdatedAt());
        return dto;
    }

    // ========== Marketplace ==========

    // Get marketplace tasks for child with optional search
    @GetMapping("/child/marketplace")
    public String marketplace(@AuthenticationPrincipal UserDetails userDetails, 
                             @RequestParam(required = false) String search,
                             Model model) {
        log.info("=== CHILD MARKETPLACE CONTROLLER INVOKED ===");
        if (userDetails == null) {
            log.warn("UserDetails is null - user not authenticated!");
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在：" + username));

        log.info("Marketplace accessed by user: {}, userId: {}, search: {}", username, user.getId(), search);

        // Get child's parent ID to filter marketplace tasks
        ChildDTO child = userService.getChildById(user.getId());
        Long parentId = child.getParentId();

        // Query marketplace tasks with optional search
        List<TaskDTO> marketplaceTasks;
        if (search != null && !search.trim().isEmpty()) {
            marketplaceTasks = taskService.getMarketplaceTasksWithSearch(user.getId(), search.trim());
            log.info("Search keyword: '{}', found {} tasks", search, marketplaceTasks.size());
        } else {
            marketplaceTasks = taskService.getMarketplaceTasks(user.getId());
        }
        model.addAttribute("marketplaceTasks", marketplaceTasks);
        model.addAttribute("searchKeyword", search != null ? search : "");

        // Get child's picked tasks
        List<TaskDTO> pickedTasks = taskService.getPickedTasks(user.getId());
        model.addAttribute("pickedTasks", pickedTasks);

        // Get child's current points for display
        Integer childPoints = child.getPoints();
        model.addAttribute("childPoints", childPoints);

        // Add username for display
        model.addAttribute("username", username);

        log.info("Found {} marketplace tasks and {} picked tasks for child: {} with {} points",
                marketplaceTasks.size(), pickedTasks.size(), username, childPoints);
        return "child/marketplace";
    }

    // Pick a task from marketplace
    @PostMapping("/child/marketplace/{taskId}/pick")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> pickTask(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable Long taskId) {
        log.info("Child picking task from marketplace: taskId={}", taskId);
        try {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            // Get child ID from user
            Long childId = user.getId();

            // Call service to pick the task
            taskService.pickTask(taskId, childId);

            log.info("Task {} picked successfully by child {}", taskId, childId);
            return ResponseEntity.ok(ApiResponse.success("任务已成功领取！", null));
        } catch (Exception e) {
            log.error("Failed to pick task", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("PICK_FAILED", e.getMessage()));
        }
    }

    // Unpick (release) a task back to marketplace
    @PostMapping("/child/marketplace/{taskId}/unpick")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> unpickTask(@AuthenticationPrincipal UserDetails userDetails,
                             @PathVariable Long taskId) {
        log.info("Child unpicking task: taskId={}", taskId);
        try {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            // Get child ID from user
            Long childId = user.getId();

            // Call service to unpick the task
            taskService.unpickTask(taskId, childId);

            log.info("Task {} unpicked successfully by child {}", taskId, childId);
            return ResponseEntity.ok(ApiResponse.success("任务已释放！", null));
        } catch (Exception e) {
            log.error("Failed to unpick task", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("UNPICK_FAILED", e.getMessage()));
        }
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

        // Get pending rewards (redeemed but not used)
        List<RewardRedemptionDTO> pendingRewards = redeemedRewards.stream()
                .filter(r -> "REDEEMED".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("pendingRewards", pendingRewards);

        // Add username for display
        model.addAttribute("username", username);

        log.info("Found {} rewards for child: {} with {} points, {} total redemptions, {} pending",
                rewards.size(), username, child.getPoints(), redeemedRewards.size(), pendingRewards.size());
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
    // ========== Reward Management ==========
    
    @GetMapping("/parent/rewards")
    public String parentRewards(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("=== PARENT REWARDS CONTROLLER INVOKED ===");
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        String username = userDetails.getUsername();
        User parent = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
        
        // Get all rewards
        List<RewardDTO> rewards = rewardService.getAllRewards();
        model.addAttribute("rewards", rewards);
        model.addAttribute("username", username);
        
        log.info("Found {} rewards for parent: {}", rewards.size(), username);
        return "parent/rewards";
    }
    
    @PostMapping("/parent/rewards")
    public String createReward(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String name,
                               @RequestParam Integer pointsRequired,
                               @RequestParam(required = false, defaultValue = "999") Integer quantity,
                               @RequestParam(required = false) String imageUrl,
                               @RequestParam(required = false) String description,
                               RedirectAttributes redirectAttrs) {
        log.info("Creating new reward: name={}, points={}", name, pointsRequired);
        try {
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(name);  // Using title field for name
            request.setPoints(pointsRequired);
            request.setDescription(description);
            
            RewardDTO reward = rewardService.createReward(request);
            
            // Update quantity and imageUrl separately since createReward doesn't support them
            if (quantity != null || imageUrl != null) {
                RewardDTO existing = rewardService.getRewardById(reward.getId());
                // Note: The service doesn't fully support updating these fields yet
                // This is a limitation - quantity defaults to 999
            }
            
            redirectAttrs.addFlashAttribute("success", "礼物创建成功！");
        } catch (Exception e) {
            log.error("Failed to create reward", e);
            redirectAttrs.addFlashAttribute("error", "创建失败: " + e.getMessage());
        }
        return "redirect:/parent/rewards";
    }
    
    @PostMapping("/parent/rewards/{id}/edit")
    public String editReward(@AuthenticationPrincipal UserDetails userDetails,
                             @PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam Integer pointsRequired,
                             @RequestParam(required = false, defaultValue = "999") Integer quantity,
                             @RequestParam(required = false) String imageUrl,
                             @RequestParam(required = false) String description,
                             RedirectAttributes redirectAttrs) {
        log.info("Editing reward: id={}, name={}, points={}, quantity={}", id, name, pointsRequired, quantity);
        try {
            String username = userDetails.getUsername();
            User parent = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            
            // Verify reward exists first
            RewardDTO existing = rewardService.getRewardById(id);
            
            // Update using CreateTaskRequest (service uses title for name, points for pointsRequired)
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(name);
            request.setPoints(pointsRequired);
            request.setDescription(description);
            
            rewardService.updateReward(id, request);
            
            // Note: quantity and imageUrl updates are not supported by the current service
            // This is a limitation
            
            redirectAttrs.addFlashAttribute("success", "礼物更新成功！");
        } catch (Exception e) {
            log.error("Failed to edit reward", e);
            redirectAttrs.addFlashAttribute("error", "更新失败: " + e.getMessage());
        }
        return "redirect:/parent/rewards";
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

    // ========== Draft Task Approval ==========

    // Get draft tasks for parent approval
    @GetMapping("/parent/drafts")
    public String draftApprovals(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("=== PARENT DRAFT APPROVALS CONTROLLER INVOKED ===");
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User parent = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

        // Get draft tasks from this parent's children
        List<TaskDTO> draftTasks = taskService.getDraftTasksByParent(parent.getId());
        model.addAttribute("draftTasks", draftTasks);

        // Get children for dropdown
        List<ChildDTO> children = userService.getChildrenByParentId(parent.getId());
        model.addAttribute("children", children);

        model.addAttribute("username", username);

        log.info("Found {} draft tasks for parent: {}", draftTasks.size(), username);
        return "parent/drafts";
    }

    // Approve draft task (DRAFT -> APPROVED)
    @PostMapping("/parent/drafts/{taskId}/approve")
    public String approveDraftTask(@AuthenticationPrincipal UserDetails userDetails,
                                   @PathVariable Long taskId,
                                   RedirectAttributes redirectAttrs) {
        log.info("Approving draft task: taskId={}", taskId);
        try {
            taskService.approveDraftTask(taskId);
            redirectAttrs.addFlashAttribute("success", "草稿任务已批准，成为正式任务！");
            return "redirect:/parent/drafts";
        } catch (Exception e) {
            log.error("Failed to approve draft task", e);
            redirectAttrs.addFlashAttribute("error", "批准失败: " + e.getMessage());
            return "redirect:/parent/drafts";
        }
    }

    // Reject draft task (DRAFT -> REJECTED)
    @PostMapping("/parent/drafts/{taskId}/reject")
    public String rejectDraftTask(@AuthenticationPrincipal UserDetails userDetails,
                                  @PathVariable Long taskId,
                                  RedirectAttributes redirectAttrs) {
        log.info("Rejecting draft task: taskId={}", taskId);
        try {
            taskService.rejectDraftTask(taskId);
            redirectAttrs.addFlashAttribute("success", "草稿任务已拒绝");
            return "redirect:/parent/drafts";
        } catch (Exception e) {
            log.error("Failed to reject draft task", e);
            redirectAttrs.addFlashAttribute("error", "拒绝失败: " + e.getMessage());
            return "redirect:/parent/drafts";
        }
    }

    // ========== Marketplace Task Management ==========

    // Hide task from marketplace (set active = false)
    @PostMapping("/parent/marketplace/{id}/hide")
    public String hideMarketplaceTask(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable Long id,
                                      RedirectAttributes redirectAttrs) {
        log.info("Hiding task from marketplace: taskId={}", id);
        try {
            // Verify current user is the task creator
            TaskDTO task = taskService.getTaskById(id);
            String username = userDetails.getUsername();
            User parent = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            Long currentUserId = parent.getId();
            if (!task.getCreatedById().equals(currentUserId)) {
                redirectAttrs.addFlashAttribute("error", "无权隐藏此任务");
                return "redirect:/parent/marketplace";
            }

            taskService.hideTask(id);
            redirectAttrs.addFlashAttribute("success", "任务已从市场隐藏，孩子将无法看到此任务");
        } catch (Exception e) {
            log.error("Failed to hide task", e);
            redirectAttrs.addFlashAttribute("error", "隐藏任务失败: " + e.getMessage());
        }
        return "redirect:/parent/marketplace";
    }

    // Unhide task to marketplace (set active = true)
    @PostMapping("/parent/marketplace/{id}/unhide")
    public String unhideMarketplaceTask(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable Long id,
                                        RedirectAttributes redirectAttrs) {
        log.info("Unhiding task to marketplace: taskId={}", id);
        try {
            // Verify current user is the task creator
            TaskDTO task = taskService.getTaskById(id);
            String username = userDetails.getUsername();
            User parent = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            Long currentUserId = parent.getId();
            if (!task.getCreatedById().equals(currentUserId)) {
                redirectAttrs.addFlashAttribute("error", "无权显示此任务");
                return "redirect:/parent/marketplace";
            }

            taskService.unhideTask(id);
            redirectAttrs.addFlashAttribute("success", "任务已重新发布到市场，孩子现在可以看到此任务");
        } catch (Exception e) {
            log.error("Failed to unhide task", e);
            redirectAttrs.addFlashAttribute("error", "显示任务失败: " + e.getMessage());
        }
        return "redirect:/parent/marketplace";
    }

    // Delete marketplace task (hard delete)
    @PostMapping("/parent/marketplace/{id}/delete")
    public String deleteMarketplaceTask(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable Long id,
                                        RedirectAttributes redirectAttrs) {
        log.info("Deleting marketplace task: taskId={}", id);
        try {
            // Verify current user is the task creator
            TaskDTO task = taskService.getTaskById(id);
            String username = userDetails.getUsername();
            User parent = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
            Long currentUserId = parent.getId();
            if (!task.getCreatedById().equals(currentUserId)) {
                redirectAttrs.addFlashAttribute("error", "无权删除此任务");
                return "redirect:/parent/marketplace";
            }

            // Check if task is currently picked by a child (has active TaskJob)
            // For now, allow deletion - TaskJob will be handled by cascade or manual cleanup

            taskService.deleteTask(id);
            redirectAttrs.addFlashAttribute("success", "任务已永久删除");
        } catch (Exception e) {
            log.error("Failed to delete marketplace task", e);
            redirectAttrs.addFlashAttribute("error", "删除任务失败: " + e.getMessage());
        }
        return "redirect:/parent/marketplace";
    }

    // ========== Penalty Notifications ==========

    // Get penalty notifications page for parent
    @GetMapping("/parent/notifications")
    public String penaltyNotifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("=== PARENT NOTIFICATIONS CONTROLLER INVOKED ===");
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User parent = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

        // Get pending and all notifications
        var pendingNotifications = taskService.getPendingPenaltyNotifications(parent.getId());
        var allNotifications = taskService.getAllPenaltyNotifications(parent.getId());

        model.addAttribute("pendingNotifications", pendingNotifications);
        model.addAttribute("allNotifications", allNotifications);
        model.addAttribute("username", username);

        log.info("Found {} pending, {} total notifications for parent: {}",
                pendingNotifications.size(), allNotifications.size(), username);
        return "parent/notifications";
    }

    // Apply penalty to a notification
    @PostMapping("/parent/notifications/{notificationId}/apply-penalty")
    public String applyPenalty(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long notificationId,
                               RedirectAttributes redirectAttrs) {
        log.info("Applying penalty for notification: notificationId={}", notificationId);
        try {
            String username = userDetails.getUsername();
            User parent = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            taskService.applyPenalty(notificationId, parent.getId());
            redirectAttrs.addFlashAttribute("success", "惩罚已执行，积分已扣除！");
            return "redirect:/parent/notifications";
        } catch (Exception e) {
            log.error("Failed to apply penalty", e);
            redirectAttrs.addFlashAttribute("error", "执行惩罚失败: " + e.getMessage());
            return "redirect:/parent/notifications";
        }
    }

    // Dismiss (ignore) a notification
    @PostMapping("/parent/notifications/{notificationId}/dismiss")
    public String dismissNotification(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable Long notificationId,
                                      RedirectAttributes redirectAttrs) {
        log.info("Dismissing notification: notificationId={}", notificationId);
        try {
            taskService.dismissPenalty(notificationId);
            redirectAttrs.addFlashAttribute("success", "已忽略此通知");
            return "redirect:/parent/notifications";
        } catch (Exception e) {
            log.error("Failed to dismiss notification", e);
            redirectAttrs.addFlashAttribute("error", "忽略失败: " + e.getMessage());
            return "redirect:/parent/notifications";
        }
    }

    // Trigger manual check for mandatory task deadlines (for testing/admin)
    @PostMapping("/parent/notifications/check-deadlines")
    public String checkDeadlines(@AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttrs) {
        log.info("Manual deadline check triggered by user: {}", userDetails.getUsername());
        try {
            String username = userDetails.getUsername();
            User parent = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            taskService.checkAndNotifyMandatoryTaskDeadline(parent.getId());
            redirectAttrs.addFlashAttribute("success", "已检查所有强制任务截止时间");
            return "redirect:/parent/notifications";
        } catch (Exception e) {
            log.error("Failed to check deadlines", e);
            redirectAttrs.addFlashAttribute("error", "检查失败：" + e.getMessage());
            return "redirect:/parent/notifications";
        }
    }

    // ========== Lottery Management (Parent) ==========

    @GetMapping("/parent/lottery")
    public String parentLottery(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("=== PARENT LOTTERY CONTROLLER INVOKED ===");
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在：" + username));

        // Get parent's lottery themes
        List<LotteryThemeDTO> themes = lotteryService.getThemesByParent(user.getId());
        model.addAttribute("themes", themes);
        model.addAttribute("username", username);

        log.info("Found {} lottery themes for parent: {}", themes.size(), username);
        return "parent/lottery";
    }

    // ========== Lottery (Child) ==========

    @GetMapping("/child/lottery")
    public String childLottery(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("=== CHILD LOTTERY CONTROLLER INVOKED ===");
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在：" + username));

        // Get active lottery themes with prizes
        List<LotteryThemeDTO> themes = lotteryService.getAllActiveThemesWithPrizes();
        model.addAttribute("themes", themes);

        ChildDTO child = userService.getChildById(user.getId());
        // Use legacy points field for display (PointWallet system is for internal tracking)
        int availablePoints = child.getPoints() != null ? child.getPoints() : 0;
        model.addAttribute("childPoints", availablePoints);
        model.addAttribute("username", username);

        log.info("Found {} active lottery themes for child: {}", themes.size(), username);
        return "child/lottery";
    }
}
