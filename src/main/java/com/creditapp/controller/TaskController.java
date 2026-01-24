package com.creditapp.controller;

import com.creditapp.dto.*;
import com.creditapp.service.TaskService;
import com.creditapp.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "任务管理API")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "创建任务")
    public ResponseEntity<ApiResponse<TaskDTO>> createTask(@Valid @RequestBody CreateTaskRequest request,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long createdById = SecurityUtils.getCurrentUserId();
        TaskDTO task = taskService.createTask(request, createdById);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @GetMapping
    @Operation(summary = "获取所有任务")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取任务")
    public ResponseEntity<ApiResponse<TaskDTO>> getTaskById(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新任务")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTask(@PathVariable Long id,
                                                         @Valid @RequestBody CreateTaskRequest request) {
        TaskDTO task = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "完成任务")
    public ResponseEntity<ApiResponse<TaskCompletionDTO>> completeTask(@PathVariable Long id,
                                                                       @AuthenticationPrincipal UserDetails userDetails) {
        Long childId = SecurityUtils.getChildIdFromUsername(userDetails.getUsername());
        TaskCompletionDTO completion = taskService.completeTask(id, childId);
        return ResponseEntity.ok(ApiResponse.success(completion));
    }

    @PostMapping("/approvals/{completionId}")
    @Operation(summary = "批准任务完成")
    public ResponseEntity<ApiResponse<TaskCompletionDTO>> approveCompletion(@PathVariable Long completionId) {
        TaskCompletionDTO completion = taskService.approveCompletion(completionId);
        return ResponseEntity.ok(ApiResponse.success(completion));
    }

    @PostMapping("/rejections/{completionId}")
    @Operation(summary = "拒绝任务完成")
    public ResponseEntity<ApiResponse<TaskCompletionDTO>> rejectCompletion(@PathVariable Long completionId) {
        TaskCompletionDTO completion = taskService.rejectCompletion(completionId);
        return ResponseEntity.ok(ApiResponse.success(completion));
    }
}