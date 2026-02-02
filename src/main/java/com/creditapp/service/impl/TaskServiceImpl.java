package com.creditapp.service.impl;

import com.creditapp.dto.*;
import com.creditapp.entity.*;
import com.creditapp.exception.BusinessException;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.repository.ChildRepository;
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
