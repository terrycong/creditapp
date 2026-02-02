package com.creditapp.service.impl;

import com.creditapp.dto.*;
import com.creditapp.entity.*;
import com.creditapp.exception.BusinessException;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.repository.ChildRepository;
import com.creditapp.repository.PenaltyNotificationRepository;
import com.creditapp.repository.TaskCompletionRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final TaskCompletionRepository taskCompletionRepository;
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
        task.setAssignedChild(assignedChild);
        task.setActive(true);

        // Set mandatory task fields if type is MANDATORY
        if (request.getType() == TaskType.MANDATORY) {
            task.setDeadlineType(request.getDeadlineType());
            task.setDeadlineValue(request.getDeadlineValue());
            task.setPenaltyPoints(request.getPenaltyPoints());
        }

        Task savedTask = taskRepository.save(task);
        return toDTO(savedTask);
    }

    @Override
    @Transactional
    public TaskDTO updateTask(Long taskId, CreateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPoints(request.getPoints());

        if (request.getAssignedChildId() != null) {
            Child assignedChild = childRepository.findById(request.getAssignedChildId())
                    .orElseThrow(() -> new ResourceNotFoundException("Child", request.getAssignedChildId()));
            task.setAssignedChild(assignedChild);
        }

        Task savedTask = taskRepository.save(task);
        return toDTO(savedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        taskRepository.delete(task);
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
        List<Task> tasks = taskRepository.findActiveTasksWithChild(childId);
        return tasks.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> getTasksByParent(Long parentId) {
        List<Task> tasks = taskRepository.findByCreatedBy_Id(parentId);
        return tasks.stream().map(this::toDTO).collect(Collectors.toList());
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
        child.setPoints(child.getPoints() + points);

        taskCompletionRepository.save(completion);
        childRepository.save(child);

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
        task.setStatus(TaskStatus.DRAFT);  // Create as DRAFT
        task.setCreatedBy(createdBy);
        task.setAssignedChild(assignedChild);
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
            if (task.getAssignedChild() == null) {
                continue;
            }

            Child child = task.getAssignedChild();
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
            child.setPoints(child.getPoints() - penalty);
            childRepository.save(child);
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
                .assignedChildId(task.getAssignedChild() != null ? task.getAssignedChild().getId() : null)
                .assignedChildName(task.getAssignedChild() != null ? task.getAssignedChild().getUsername() : null)
                .active(task.isActive())
                .deadlineType(task.getDeadlineType())
                .deadlineValue(task.getDeadlineValue())
                .penaltyPoints(task.getPenaltyPoints())
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
}
