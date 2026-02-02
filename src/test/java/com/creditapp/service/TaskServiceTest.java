package com.creditapp.service;

import com.creditapp.dto.CreateTaskRequest;
import com.creditapp.dto.TaskDTO;
import com.creditapp.dto.TaskCompletionDTO;
import com.creditapp.entity.*;
import com.creditapp.exception.BusinessException;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.repository.ChildRepository;
import com.creditapp.repository.TaskCompletionRepository;
import com.creditapp.repository.TaskRepository;
import com.creditapp.repository.UserRepository;
import com.creditapp.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCompletionRepository taskCompletionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChildRepository childRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User parentUser;
    private Child childUser;
    private Task task;
    private CreateTaskRequest createTaskRequest;

    @BeforeEach
    void setUp() {
        // Setup parent user
        parentUser = new User();
        parentUser.setId(1L);
        parentUser.setUsername("parent");
        parentUser.setRole(UserRole.PARENT);

        // Setup child user
        childUser = new Child();
        childUser.setId(2L);
        childUser.setUsername("child");
        childUser.setRole(UserRole.CHILD);
        childUser.setPoints(100);
        childUser.setParent(parentUser);

        // Setup task
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setPoints(10);
        task.setType(TaskType.ONE_TIME);
        task.setStatus(TaskStatus.APPROVED);
        task.setCreatedBy(parentUser);
        task.setAssignedChild(childUser);
        task.setActive(true);

        // Setup create task request
        createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTitle("Test Task");
        createTaskRequest.setDescription("Test Description");
        createTaskRequest.setPoints(10);
        createTaskRequest.setType(TaskType.ONE_TIME);
        createTaskRequest.setAssignedChildId(2L);
    }

    @Test
    void createTask_WithValidRequest_ShouldReturnTaskDTO() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(parentUser));
        when(childRepository.findById(2L)).thenReturn(Optional.of(childUser));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        TaskDTO result = taskService.createTask(createTaskRequest, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Task");
        assertThat(result.getPoints()).isEqualTo(10);
        assertThat(result.getType()).isEqualTo(TaskType.ONE_TIME);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.APPROVED);
        assertThat(result.getCreatedById()).isEqualTo(1L);
        assertThat(result.getAssignedChildId()).isEqualTo(2L);
        assertThat(result.isActive()).isTrue();

        verify(userRepository).findById(1L);
        verify(childRepository).findById(2L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_WithoutAssignedChild_ShouldReturnTaskDTO() {
        // Given
        createTaskRequest.setAssignedChildId(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(parentUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setId(1L);
            return savedTask;
        });

        // When
        TaskDTO result = taskService.createTask(createTaskRequest, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Task");
        assertThat(result.getAssignedChildId()).isNull();

        verify(userRepository).findById(1L);
        verify(childRepository, never()).findById(anyLong());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_WithNonExistentUser_ShouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(createTaskRequest, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");

        verify(userRepository).findById(99L);
        verify(childRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_WithNonExistentChild_ShouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(parentUser));
        when(childRepository.findById(99L)).thenReturn(Optional.empty());
        createTaskRequest.setAssignedChildId(99L);

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(createTaskRequest, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Child not found with id: 99");

        verify(userRepository).findById(1L);
        verify(childRepository).findById(99L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getTaskById_WithExistingTask_ShouldReturnTaskDTO() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // When
        TaskDTO result = taskService.getTaskById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Task");
        assertThat(result.getDescription()).isEqualTo("Test Description");

        verify(taskRepository).findById(1L);
    }

    @Test
    void getTaskById_WithNonExistentTask_ShouldThrowResourceNotFoundException() {
        // Given
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: 99");

        verify(taskRepository).findById(99L);
    }

    @Test
    void getTasksByParent_ShouldReturnTaskDTOList() {
        // Given
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Another Task");
        task2.setPoints(5);
        task2.setType(TaskType.DAILY_ONCE);
        task2.setStatus(TaskStatus.APPROVED);
        task2.setCreatedBy(parentUser);
        task2.setActive(true);

        List<Task> tasks = Arrays.asList(task, task2);
        when(taskRepository.findByCreatedBy_Id(1L)).thenReturn(tasks);

        // When
        List<TaskDTO> result = taskService.getTasksByParent(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Task");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("Another Task");

        verify(taskRepository).findByCreatedBy_Id(1L);
    }

    @Test
    void getTasksByParent_WithNoTasks_ShouldReturnEmptyList() {
        // Given
        when(taskRepository.findByCreatedBy_Id(1L)).thenReturn(Arrays.asList());

        // When
        List<TaskDTO> result = taskService.getTasksByParent(1L);

        // Then
        assertThat(result).isEmpty();

        verify(taskRepository).findByCreatedBy_Id(1L);
    }

    @Test
    void getAllTasks_ShouldReturnAllTaskDTOs() {
        // Given
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Another Task");
        task2.setPoints(5);
        task2.setType(TaskType.DAILY_ONCE);
        task2.setStatus(TaskStatus.APPROVED);
        task2.setCreatedBy(parentUser);
        task2.setActive(true);

        List<Task> tasks = Arrays.asList(task, task2);
        when(taskRepository.findAll()).thenReturn(tasks);

        // When
        List<TaskDTO> result = taskService.getAllTasks();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);

        verify(taskRepository).findAll();
    }

    @Test
    void updateTask_WithValidRequest_ShouldReturnUpdatedTaskDTO() {
        // Given
        CreateTaskRequest updateRequest = new CreateTaskRequest();
        updateRequest.setTitle("Updated Task");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPoints(20);
        updateRequest.setType(TaskType.REPEATABLE);
        updateRequest.setAssignedChildId(2L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(childRepository.findById(2L)).thenReturn(Optional.of(childUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            return savedTask;
        });

        // When
        TaskDTO result = taskService.updateTask(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Task");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getPoints()).isEqualTo(20);
        // Note: Current implementation doesn't update task type
        assertThat(result.getType()).isEqualTo(TaskType.ONE_TIME); // Original type

        verify(taskRepository).findById(1L);
        verify(childRepository).findById(2L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void deleteTask_WithExistingTask_ShouldDeleteSuccessfully() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository).findById(1L);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_WithNonExistentTask_ShouldThrowResourceNotFoundException() {
        // Given
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: 99");

        verify(taskRepository).findById(99L);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void completeTask_WithValidData_ShouldReturnCompletionDTO() {
        // Given
        TaskCompletion completion = new TaskCompletion();
        completion.setId(1L);
        completion.setTask(task);
        completion.setChild(childUser);
        completion.setStatus(CompletionStatus.PENDING);
        completion.setCompletedAt(LocalDateTime.now());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(childRepository.findById(2L)).thenReturn(Optional.of(childUser));
        when(taskCompletionRepository.save(any(TaskCompletion.class))).thenReturn(completion);

        // When
        TaskCompletionDTO result = taskService.completeTask(1L, 2L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTaskId()).isEqualTo(1L);
        assertThat(result.getChildId()).isEqualTo(2L);
        assertThat(result.getStatus()).isEqualTo(CompletionStatus.PENDING);

        verify(taskRepository).findById(1L);
        verify(childRepository).findById(2L);
        verify(taskCompletionRepository).save(any(TaskCompletion.class));
    }

    @Test
    void completeTask_WithInactiveTask_ShouldThrowBusinessException() {
        // Given
        task.setActive(false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(childRepository.findById(2L)).thenReturn(Optional.of(childUser));

        // When & Then
        assertThatThrownBy(() -> taskService.completeTask(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("任务未激活");

        verify(taskRepository).findById(1L);
        verify(childRepository).findById(2L);
        verify(taskCompletionRepository, never()).save(any(TaskCompletion.class));
    }

    @Test
    void approveTask_ShouldReturnApprovedTaskDTO() {
        // Given
        task.setStatus(TaskStatus.DRAFT);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        TaskDTO result = taskService.approveTask(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TaskStatus.APPROVED);

        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void rejectTask_ShouldReturnRejectedTaskDTO() {
        // Given
        task.setStatus(TaskStatus.DRAFT);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        TaskDTO result = taskService.rejectTask(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TaskStatus.REJECTED);

        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void completeTask_WithDailyOnceTask_FirstCompletionShouldSucceed() {
        // Given
        task.setType(TaskType.DAILY_ONCE);
        TaskCompletion completion = new TaskCompletion();
        completion.setId(1L);
        completion.setTask(task);
        completion.setChild(childUser);
        completion.setStatus(CompletionStatus.PENDING);
        completion.setCompletedAt(LocalDateTime.now());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(childRepository.findById(2L)).thenReturn(Optional.of(childUser));
        when(taskCompletionRepository.existsCompletionToday(anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(taskCompletionRepository.save(any(TaskCompletion.class))).thenReturn(completion);

        // When
        TaskCompletionDTO result = taskService.completeTask(1L, 2L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(CompletionStatus.PENDING);

        verify(taskCompletionRepository).existsCompletionToday(eq(2L), eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(taskCompletionRepository).save(any(TaskCompletion.class));
    }

    @Test
    void completeTask_WithDailyOnceTask_SecondCompletionSameDayShouldThrowBusinessException() {
        // Given
        task.setType(TaskType.DAILY_ONCE);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(childRepository.findById(2L)).thenReturn(Optional.of(childUser));
        when(taskCompletionRepository.existsCompletionToday(anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> taskService.completeTask(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("该任务每天只能完成一次，请明天再试！")
                .hasFieldOrPropertyWithValue("errorCode", "DAILY_LIMIT_EXCEEDED");

        verify(taskCompletionRepository).existsCompletionToday(eq(2L), eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(taskCompletionRepository, never()).save(any(TaskCompletion.class));
    }

    @Test
    void completeTask_WithNonDailyOnceTask_ShouldNotCheckDailyLimit() {
        // Given
        task.setType(TaskType.ONE_TIME); // Already ONE_TIME in setup
        TaskCompletion completion = new TaskCompletion();
        completion.setId(1L);
        completion.setTask(task);
        completion.setChild(childUser);
        completion.setStatus(CompletionStatus.PENDING);
        completion.setCompletedAt(LocalDateTime.now());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(childRepository.findById(2L)).thenReturn(Optional.of(childUser));
        when(taskCompletionRepository.save(any(TaskCompletion.class))).thenReturn(completion);

        // When
        TaskCompletionDTO result = taskService.completeTask(1L, 2L);

        // Then
        assertThat(result).isNotNull();
        verify(taskCompletionRepository, never()).existsCompletionToday(anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(taskCompletionRepository).save(any(TaskCompletion.class));
    }
}