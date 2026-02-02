package com.creditapp.service;

import com.creditapp.dto.*;
import com.creditapp.entity.NotificationStatus;
import com.creditapp.entity.PenaltyNotification;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {
    TaskDTO createTask(CreateTaskRequest request, Long createdById);
    TaskDTO updateTask(Long taskId, CreateTaskRequest request);
    void deleteTask(Long taskId);
    TaskDTO approveTask(Long taskId);
    TaskDTO rejectTask(Long taskId);
    TaskDTO getTaskById(Long id);
    List<TaskDTO> getAllTasks();
    List<TaskDTO> getTasksByChild(Long childId);
    List<TaskDTO> getTasksByParent(Long parentId);
    TaskCompletionDTO completeTask(Long taskId, Long childId);
    TaskCompletionDTO approveCompletion(Long completionId);
    TaskCompletionDTO rejectCompletion(Long completionId);
    List<TaskCompletionDTO> getPendingCompletionsByParent(Long parentId);
    void withdrawCompletion(Long completionId, Long childId);

    // Draft task methods
    TaskDTO createDraftTask(CreateTaskRequest request, Long createdById);
    List<TaskDTO> getDraftTasksByParent(Long parentId);
    List<TaskDTO> getDraftTasksByChild(Long childId);
    TaskDTO approveDraftTask(Long taskId);
    TaskDTO rejectDraftTask(Long taskId);

    // Mandatory task methods
    int getMandatoryTaskCompletionCount(Long taskId, Long childId, LocalDateTime startDate, LocalDateTime endDate);
    void checkAndNotifyMandatoryTaskDeadline(Long parentId);
    List<PenaltyNotification> getPendingPenaltyNotifications(Long parentId);
    List<PenaltyNotification> getAllPenaltyNotifications(Long parentId);
    void applyPenalty(Long notificationId, Long appliedById);
    void dismissPenalty(Long notificationId);
    long countPendingPenaltyNotifications(Long parentId);
}
