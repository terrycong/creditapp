package com.creditapp.service.impl;

import com.creditapp.dto.*;
import com.creditapp.entity.*;
import com.creditapp.exception.BusinessException;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.repository.ChildRepository;
import com.creditapp.repository.PenaltyNotificationRepository;
import com.creditapp.repository.PointHistoryRepository;
import com.creditapp.repository.TaskCompletionRepository;
import com.creditapp.repository.TaskJobRepository;
import com.creditapp.repository.TaskRepository;
import com.creditapp.repository.UserRepository;
import com.creditapp.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final TaskJobRepository taskJobRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PenaltyNotificationRepository penaltyNotificationRepository;
    private final UserRepository userRepository;
    private final ChildRepository childRepository;

    @Override
    @Transactional
    public TaskDTO createTask(CreateTaskRequest request, Long createdById) {
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("User", createdById));

        Child assignedChild = null;
        if (request.getAssignedChildId() != null) {
            assignedChild = childRepository.findById(request.getAssignedChildId())
                    .orElseThrow(() -> new ResourceNotFoundException("Child", request.getAssignedChildId()));
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPoints(request.getPoints());
        task.setType(request.getType());
        task.setStatus(TaskStatus.APPROVED);
        task.setCreatedBy(createdBy);
        task.setActive(true);

        // Set mandatory task fields if type is MANDATORY
        if (request.getType() == TaskType.MANDATORY) {
            task.setDeadlineType(request.getDeadlineType());
            task.setDeadlineValue(request.getDeadlineValue());
            task.setPenaltyPoints(request.getPenaltyPoints());
        }

        Task savedTask = taskRepository.save(task);

        // Create TaskJob if task is assigned to a child
        if (assignedChild != null) {
            createTaskJob(savedTask, assignedChild);
        }

        return toDTO(savedTask);
    }

    /**
     * Create a TaskJob for task assignment
     */
    private void createTaskJob(Task task, Child child) {
        TaskJob job = TaskJob.builder()
                .task(task)
                .child(child)
                .status(JobStatus.ASSIGNED)
                .snapshotTitle(task.getTitle())
                .snapshotDescription(task.getDescription())
                .snapshotPoints(task.getPoints())
                .snapshotTaskType(task.getType())
                .assignedAt(LocalDateTime.now())
                .build();
        taskJobRepository.save(job);
        log.info("Created TaskJob for task {} assigned to child {}", task.getId(), child.getId());
    }

    @Override
    @Transactional
    public TaskDTO updateTask(Long taskId, CreateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPoints(request.getPoints());

        Task savedTask = taskRepository.save(task);
        return toDTO(savedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        
        List<TaskCompletion> completions = taskCompletionRepository.findByTaskId(taskId);
        if (!completions.isEmpty()) {
            taskCompletionRepository.deleteAll(completions);
            log.info("Deleted {} task completions for task {}", completions.size(), taskId);
        }
        
        List<TaskJob> jobs = taskJobRepository.findByTaskId(taskId);
        if (!jobs.isEmpty()) {
            taskJobRepository.deleteAll(jobs);
            log.info("Deleted {} task jobs for task {}", jobs.size(), taskId);
        }
        
        List<PenaltyNotification> notifications = penaltyNotificationRepository.findByTaskId(taskId);
        if (!notifications.isEmpty()) {
            penaltyNotificationRepository.deleteAll(notifications);
            log.info("Deleted {} penalty notifications for task {}", notifications.size(), taskId);
        }
        
        taskRepository.delete(task);
        log.info("Task {} deleted successfully", taskId);
    }

    @Override
    @Transactional
    public TaskDTO approveTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        task.setStatus(TaskStatus.APPROVED);
        return toDTO(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskDTO rejectTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        task.setStatus(TaskStatus.REJECTED);
        return toDTO(taskRepository.save(task));
    }

    @Override
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        return toDTO(task);
    }

    @Override
    public List<TaskDTO> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> getTasksByChild(Long childId) {
        // Get active TaskJobs for this child
        List<TaskJob> jobs = taskJobRepository.findActiveJobsByChildId(childId);
        return jobs.stream()
                .map(this::jobToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert TaskJob to TaskDTO (uses snapshot fields)
     */
    private TaskDTO jobToDTO(TaskJob job) {
        return TaskDTO.builder()
                .id(job.getTask().getId())
                .title(job.getSnapshotTitle())
                .description(job.getSnapshotDescription())
                .points(job.getSnapshotPoints())
                .type(job.getSnapshotTaskType())
                .status(job.getTask().getStatus())
                .createdById(job.getTask().getCreatedBy() != null ? job.getTask().getCreatedBy().getId() : null)
                .createdByName(job.getTask().getCreatedBy() != null ? job.getTask().getCreatedBy().getUsername() : null)
                .active(job.getStatus() == JobStatus.ASSIGNED || job.getStatus() == JobStatus.IN_PROGRESS)
                .createdAt(job.getTask().getCreatedAt())
                .deadlineType(job.getTask().getDeadlineType())
                .deadlineValue(job.getTask().getDeadlineValue())
                .penaltyPoints(job.getTask().getPenaltyPoints())
                .build();
    }

    @Override
    public List<TaskDTO> getTasksByParent(Long parentId) {
        List<Task> tasks = taskRepository.findByCreatedBy_Id(parentId);
        
        // Build a map of taskId -> assignedChildName for tasks that have TaskJob
        java.util.Map<Long, String> taskAssignmentMap = new java.util.HashMap<>();
        List<TaskJob> jobs = taskJobRepository.findJobsByParentId(parentId);
        for (TaskJob job : jobs) {
            // Only store if not already present (keep first assignment)
            taskAssignmentMap.putIfAbsent(job.getTask().getId(), job.getChild().getUsername());
        }
        
        // Convert to DTO with assignment info
        return tasks.stream()
                .map(task -> toDTOWithAssignedInfo(task, taskAssignmentMap.get(task.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskCompletionDTO completeTask(Long taskId, Long childId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", childId));

        if (!task.isActive()) {
            throw new BusinessException("TASK_INACTIVE", "任务未激活");
        }

        // Find the TaskJob for this task and child
        TaskJob job = taskJobRepository.findByTaskIdAndChildId(taskId, childId)
                .orElseThrow(() -> new BusinessException("TASK_NOT_ASSIGNED", "任务未分配给该孩子"));

        // Update job status to IN_PROGRESS if it's the first completion
        if (job.getStatus() == JobStatus.ASSIGNED) {
            job.setStatus(JobStatus.IN_PROGRESS);
            job.setStartedAt(LocalDateTime.now());
            taskJobRepository.save(job);
        }

        // Check for DAILY_ONCE task - child can only complete once per day
        if (task.getType() == TaskType.DAILY_ONCE) {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            boolean completedToday = taskCompletionRepository.existsCompletionToday(
                    childId, taskId, startOfDay, endOfDay);
            if (completedToday) {
                throw new BusinessException("DAILY_LIMIT_EXCEEDED",
                        "该任务每天只能完成一次，请明天再试！");
            }
        }

        TaskCompletion completion = new TaskCompletion();
        completion.setTask(task);
        completion.setTaskJob(job);
        completion.setChild(child);
        completion.setStatus(CompletionStatus.PENDING);
        completion.setCompletedAt(LocalDateTime.now());

        TaskCompletion saved = taskCompletionRepository.save(completion);
        log.info("Task {} completed by child {}, waiting for approval", taskId, childId);
        return toCompletionDTO(saved);
    }

    @Override
    @Transactional
    public TaskCompletionDTO approveCompletion(Long completionId) {
        TaskCompletion completion = taskCompletionRepository.findById(completionId)
                .orElseThrow(() -> new ResourceNotFoundException("TaskCompletion", completionId));

        if (completion.getStatus() != CompletionStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "任务不在待审核状态");
        }

        completion.setStatus(CompletionStatus.APPROVED);
        completion.setApprovedAt(LocalDateTime.now());

        int points = completion.getTask().getPoints();
        Child child = completion.getChild();
        int originalPoints = child.getPoints();
        child.setPoints(child.getPoints() + points);

        // Update TaskJob status to COMPLETED if linked
        if (completion.getTaskJob() != null) {
            TaskJob job = completion.getTaskJob();
            job.setStatus(JobStatus.COMPLETED);
            taskJobRepository.save(job);
        }

        taskCompletionRepository.save(completion);
        childRepository.save(child);

        // Record point history
        PointHistory history = PointHistory.builder()
                .child(child)
                .originalPoints(originalPoints)
                .changePoints(points)
                .afterPoints(child.getPoints())
                .changeType(PointChangeType.TASK_COMPLETION)
                .description("完成任务: " + completion.getTask().getTitle())
                .referenceId(completion.getId())
                .referenceType("TASK_COMPLETION")
                .changedById(null)
                .createdAt(LocalDateTime.now())
                .build();
        pointHistoryRepository.save(history);

        log.info("Approved task completion {}, child {} earned {} points", completionId, child.getId(), points);
        return toCompletionDTO(completion);
    }

    @Override
    @Transactional
    public TaskCompletionDTO rejectCompletion(Long completionId) {
        TaskCompletion completion = taskCompletionRepository.findById(completionId)
                .orElseThrow(() -> new ResourceNotFoundException("TaskCompletion", completionId));
        
        if (completion.getStatus() != CompletionStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "任务不在待审核状态");
        }
        
        completion.setStatus(CompletionStatus.REJECTED);
        TaskCompletion saved = taskCompletionRepository.save(completion);
        
        log.info("Rejected task completion {}", completionId);
        return toCompletionDTO(saved);
    }

    @Override
    @Transactional
    public void withdrawCompletion(Long completionId, Long childId) {
        TaskCompletion completion = taskCompletionRepository.findById(completionId)
                .orElseThrow(() -> new ResourceNotFoundException("TaskCompletion", completionId));

        // Verify the completion belongs to this child
        if (!completion.getChild().getId().equals(childId)) {
            throw new BusinessException("PERMISSION_DENIED", "无权撤回此完成申请");
        }

        // Only allow withdrawing if status is PENDING
        if (completion.getStatus() != CompletionStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "只能撤回待审批的申请");
        }

        taskCompletionRepository.delete(completion);
        log.info("Withdrawn task completion {} by child {}", completionId, childId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskCompletionDTO> getPendingCompletionsByParent(Long parentId) {
        List<TaskCompletion> completions = taskCompletionRepository.findPendingCompletionsByParentId(parentId);
        return completions.stream()
                .map(this::toCompletionDTO)
                .collect(Collectors.toList());
    }

    // ========== Draft Task Methods ==========

    @Override
    @Transactional
    public TaskDTO createDraftTask(CreateTaskRequest request, Long createdById) {
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("User", createdById));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPoints(request.getPoints());
        task.setType(request.getType());
        task.setStatus(TaskStatus.DRAFT);  // Create as DRAFT
        task.setCreatedBy(createdBy);
        task.setActive(true);

        Task savedTask = taskRepository.save(task);
        log.info("Draft task created by user {}: taskId={}", createdById, savedTask.getId());
        return toDTO(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> getDraftTasksByParent(Long parentId) {
        List<Task> tasks = taskRepository.findDraftTasksByParentId(parentId);
        return tasks.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> getDraftTasksByChild(Long childId) {
        List<Task> tasks = taskRepository.findByCreatedById(childId);
        return tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DRAFT)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskDTO approveDraftTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        if (task.getStatus() != TaskStatus.DRAFT) {
            throw new BusinessException("INVALID_STATUS", "任务不是草稿状态");
        }

        task.setStatus(TaskStatus.APPROVED);
        Task savedTask = taskRepository.save(task);
        log.info("Draft task approved: taskId={}", taskId);
        return toDTO(savedTask);
    }

    @Override
    @Transactional
    public TaskDTO rejectDraftTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        if (task.getStatus() != TaskStatus.DRAFT) {
            throw new BusinessException("INVALID_STATUS", "任务不是草稿状态");
        }

        task.setStatus(TaskStatus.REJECTED);
        Task savedTask = taskRepository.save(task);
        log.info("Draft task rejected: taskId={}", taskId);
        return toDTO(savedTask);
    }

    // ========== Mandatory Task Methods ==========

    @Override
    @Transactional(readOnly = true)
    public int getMandatoryTaskCompletionCount(Long taskId, Long childId, LocalDateTime startDate, LocalDateTime endDate) {
        return taskCompletionRepository.countApprovedCompletionsInDateRange(taskId, childId, startDate, endDate);
    }

    @Override
    @Transactional
    public void checkAndNotifyMandatoryTaskDeadline(Long parentId) {
        log.info("Checking mandatory task deadlines for parentId={}", parentId);

        // Find all MANDATORY tasks created by this parent
        List<Task> mandatoryTasks = taskRepository.findByCreatedBy_Id(parentId).stream()
                .filter(t -> t.getType() == TaskType.MANDATORY && t.isActive())
                .collect(Collectors.toList());

        for (Task task : mandatoryTasks) {
            // Find active TaskJobs for this task
            List<TaskJob> activeJobs = taskJobRepository.findByTaskId(task.getId()).stream()
                    .filter(j -> j.getStatus() == JobStatus.ASSIGNED || j.getStatus() == JobStatus.IN_PROGRESS)
                    .toList();

            for (TaskJob job : activeJobs) {
                Child child = job.getChild();
                LocalDate today = LocalDate.now();
                LocalDateTime startOfDay = today.atStartOfDay();
                LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

                // Count completions for today
                int completionsToday = taskCompletionRepository.countApprovedCompletionsInDateRange(
                        task.getId(), child.getId(), startOfDay, endOfDay);

                // For DAILY deadline type, check if completed today
                if (task.getDeadlineType() == TaskDeadlineType.DAILY) {
                    if (completionsToday == 0) {
                        // Not completed today, create notification if not already exists
                        if (!penaltyNotificationRepository.existsPendingNotification(task.getId(), child.getId())) {
                            User parent = task.getCreatedBy();
                            PenaltyNotification notification = new PenaltyNotification(task, child, parent, task.getPenaltyPoints());
                            penaltyNotificationRepository.save(notification);
                            log.info("Created penalty notification for mandatory task {} not completed by child {}",
                                    task.getId(), child.getId());
                        }
                    }
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaltyNotification> getPendingPenaltyNotifications(Long parentId) {
        return penaltyNotificationRepository.findByParentIdAndStatusOrderByNotificationTimeDesc(parentId, NotificationStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaltyNotification> getAllPenaltyNotifications(Long parentId) {
        return penaltyNotificationRepository.findByParentIdOrderByNotificationTimeDesc(parentId);
    }

    @Override
    @Transactional
    public void applyPenalty(Long notificationId, Long appliedById) {
        PenaltyNotification notification = penaltyNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("PenaltyNotification", notificationId));

        if (notification.getStatus() != NotificationStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "通知不在待处理状态");
        }

        // Apply penalty points to child
        Child child = notification.getChild();
        int penalty = notification.getPenaltyPoints() != null ? notification.getPenaltyPoints() : 0;
        if (penalty > 0) {
            int originalPoints = child.getPoints();
            child.setPoints(child.getPoints() - penalty);
            childRepository.save(child);

            // Record point history
            PointHistory history = PointHistory.builder()
                    .child(child)
                    .originalPoints(originalPoints)
                    .changePoints(-penalty)
                    .afterPoints(child.getPoints())
                    .changeType(PointChangeType.PENALTY)
                    .description("未完成任务惩罚: " + notification.getTask().getTitle())
                    .referenceId(notificationId)
                    .referenceType("PENALTY")
                    .changedById(appliedById)
                    .createdAt(LocalDateTime.now())
                    .build();
            pointHistoryRepository.save(history);

            log.info("Applied penalty of {} points to child {}", penalty, child.getId());
        }

        // Update notification status
        notification.setPenaltyApplied(true);
        notification.setStatus(NotificationStatus.APPLIED);
        notification.setAppliedAt(LocalDateTime.now());
        notification.setAppliedById(appliedById);
        penaltyNotificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void dismissPenalty(Long notificationId) {
        PenaltyNotification notification = penaltyNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("PenaltyNotification", notificationId));

        if (notification.getStatus() != NotificationStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "通知不在待处理状态");
        }

        notification.setStatus(NotificationStatus.DISMISSED);
        penaltyNotificationRepository.save(notification);
        log.info("Dismissed penalty notification {}", notificationId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingPenaltyNotifications(Long parentId) {
        return penaltyNotificationRepository.countByParentIdAndStatus(parentId, NotificationStatus.PENDING);
    }

    private TaskDTO toDTO(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .points(task.getPoints())
                .type(task.getType())
                .status(task.getStatus())
                .createdById(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null)
                .createdByName(task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : null)
                .active(task.isActive())
                .createdAt(task.getCreatedAt())
                .deadlineType(task.getDeadlineType())
                .deadlineValue(task.getDeadlineValue())
                .penaltyPoints(task.getPenaltyPoints())
                .build();
    }

    private TaskDTO toDTOWithPickedInfo(Task task, String pickedByChildName) {
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .points(task.getPoints())
                .type(task.getType())
                .status(task.getStatus())
                .createdById(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null)
                .createdByName(task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : null)
                .active(task.isActive())
                .createdAt(task.getCreatedAt())
                .deadlineType(task.getDeadlineType())
                .deadlineValue(task.getDeadlineValue())
                .penaltyPoints(task.getPenaltyPoints())
                .pickedByChildName(pickedByChildName)
                .build();
    }

    private TaskDTO toDTOWithAssignedInfo(Task task, String assignedChildName) {
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .points(task.getPoints())
                .type(task.getType())
                .status(task.getStatus())
                .createdById(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null)
                .createdByName(task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : null)
                .active(task.isActive())
                .createdAt(task.getCreatedAt())
                .deadlineType(task.getDeadlineType())
                .deadlineValue(task.getDeadlineValue())
                .penaltyPoints(task.getPenaltyPoints())
                .assignedChildName(assignedChildName)
                .build();
    }

    private TaskCompletionDTO toCompletionDTO(TaskCompletion completion) {
        return TaskCompletionDTO.builder()
                .id(completion.getId())
                .taskId(completion.getTask().getId())
                .taskTitle(completion.getTask().getTitle())
                .taskPoints(completion.getTask().getPoints())
                .childId(completion.getChild().getId())
                .childName(completion.getChild().getUsername())
                .status(completion.getStatus())
                .proof(completion.getProof())
                .completedAt(completion.getCompletedAt())
                .approvedAt(completion.getApprovedAt())
                .build();
    }

    // ========== Pick/Unpick Task Methods ==========

    @Override
    @Transactional
    public TaskDTO pickTask(Long taskId, Long childId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", childId));

        // Validate task belongs to child's family
        if (task.getCreatedBy() == null) {
            throw new BusinessException("TASK_INVALID", "任务创建者信息缺失");
        }

        // Get child's parent
        User childParent = child.getParent();
        if (childParent == null) {
            throw new BusinessException("CHILD_INVALID", "孩子没有关联的家长");
        }

        // Check if task was created by child's parent
        if (!task.getCreatedBy().getId().equals(childParent.getId())) {
            throw new BusinessException("TASK_NOT_IN_FAMILY", "不能认领其他家庭的任务");
        }

        // Check if task is already picked by another child (via TaskJob)
        boolean alreadyPicked = taskJobRepository.existsActiveJobByTaskIdAndChildId(taskId, childId);
        if (alreadyPicked) {
            throw new BusinessException("TASK_ALREADY_PICKED", "任务已被其他孩子认领");
        }

        // Create TaskJob instead of updating Task.pickedByChild
        TaskJob job = TaskJob.builder()
                .task(task)
                .child(child)
                .status(JobStatus.ASSIGNED)
                .snapshotTitle(task.getTitle())
                .snapshotDescription(task.getDescription())
                .snapshotPoints(task.getPoints())
                .snapshotTaskType(task.getType())
                .assignedAt(LocalDateTime.now())
                .build();
        taskJobRepository.save(job);

        log.info("Task {} picked by child {}", taskId, childId);
        return toDTO(task);
    }

    @Override
    @Transactional
    public void unpickTask(Long taskId, Long childId) {
        TaskJob job = taskJobRepository.findByTaskIdAndChildId(taskId, childId)
                .orElseThrow(() -> new BusinessException("TASK_NOT_PICKED", "任务未被认领"));

        // Validate job status - only allow unpicking ASSIGNED jobs
        if (job.getStatus() != JobStatus.ASSIGNED) {
            throw new BusinessException("INVALID_STATUS", "已开始的任务不能取消认领");
        }

        // Cancel the job
        job.setStatus(JobStatus.CANCELLED);
        taskJobRepository.save(job);
        log.info("Task {} unpicked by child {}", taskId, childId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> getMarketplaceTasks(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", childId));

        User parent = child.getParent();
        if (parent == null) {
            throw new BusinessException("CHILD_INVALID", "孩子没有关联的家长");
        }

        List<Task> tasks = taskRepository.findAvailableMarketplaceTasksByParentId(parent.getId());
        return tasks.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> getMarketplaceTasksWithSearch(Long childId, String keyword) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", childId));

        User parent = child.getParent();
        if (parent == null) {
            throw new BusinessException("CHILD_INVALID", "孩子没有关联的家长");
        }

        List<Task> tasks;
        if (keyword != null && !keyword.trim().isEmpty()) {
            tasks = taskRepository.findVisibleMarketplaceTasksByParentIdWithSearch(parent.getId(), keyword.trim());
        } else {
            tasks = taskRepository.findVisibleMarketplaceTasksByParentId(parent.getId());
        }
        return tasks.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> getPickedTasks(Long childId) {
        List<TaskJob> jobs = taskJobRepository.findPickedJobsByChildId(childId);
        return jobs.stream()
                .map(this::jobToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> getMarketplaceTasksByParent(Long parentId) {
        // Get all marketplace tasks including hidden/inactive ones for parent management view
        List<Task> tasks = taskRepository.findAllMarketplaceTasksIncludingHiddenByParentId(parentId);
        
        // Build a map of taskId -> pickedByChildName for tasks that have been picked
        java.util.Map<Long, String> taskPickMap = new java.util.HashMap<>();
        List<TaskJob> activeJobs = taskJobRepository.findActiveJobsByParentId(parentId);
        for (TaskJob job : activeJobs) {
            taskPickMap.put(job.getTask().getId(), job.getChild().getUsername());
        }
        
        // Convert to DTO with picked info
        return tasks.stream()
                .map(task -> toDTOWithPickedInfo(task, taskPickMap.get(task.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskDTO hideTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        task.setActive(false);
        log.info("Task {} hidden from children", taskId);
        return toDTO(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskDTO unhideTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        task.setActive(true);
        log.info("Task {} unhidden, now visible to children", taskId);
        return toDTO(taskRepository.save(task));
    }
}
