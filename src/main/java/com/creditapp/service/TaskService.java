package com.creditapp.service;

import com.creditapp.dto.*;

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
}
