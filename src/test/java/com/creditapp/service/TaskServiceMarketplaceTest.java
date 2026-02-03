package com.creditapp.service;

import com.creditapp.dto.TaskDTO;
import com.creditapp.entity.Child;
import com.creditapp.entity.Task;
import com.creditapp.entity.TaskType;
import com.creditapp.entity.TaskStatus;
import com.creditapp.entity.User;
import com.creditapp.entity.UserRole;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceMarketplaceTest {

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
    private User otherParentUser;
    private Child childUser;
    private Child otherChildUser;
    private Task marketplaceTask;
    private Task assignedTask;

    @BeforeEach
    void setUp() {
        // Setup parent user
        parentUser = new User();
        parentUser.setId(1L);
        parentUser.setUsername("parent");
        parentUser.setRole(UserRole.PARENT);

        // Setup other parent user (different family)
        otherParentUser = new User();
        otherParentUser.setId(2L);
        otherParentUser.setUsername("otherParent");
        otherParentUser.setRole(UserRole.PARENT);

        // Setup child user
        childUser = new Child();
        childUser.setId(10L);
        childUser.setUsername("child");
        childUser.setRole(UserRole.CHILD);
        childUser.setPoints(100);
        childUser.setParent(parentUser);

        // Setup other child user (different family)
        otherChildUser = new Child();
        otherChildUser.setId(20L);
        otherChildUser.setUsername("otherChild");
        otherChildUser.setRole(UserRole.CHILD);
        otherChildUser.setPoints(50);
        otherChildUser.setParent(otherParentUser);

        // Setup marketplace task (no assigned child)
        marketplaceTask = new Task();
        marketplaceTask.setId(100L);
        marketplaceTask.setTitle("Marketplace Task");
        marketplaceTask.setDescription("Available in marketplace");
        marketplaceTask.setPoints(15);
        marketplaceTask.setType(TaskType.REPEATABLE);
        marketplaceTask.setStatus(TaskStatus.APPROVED);
        marketplaceTask.setCreatedBy(parentUser);
        marketplaceTask.setAssignedChild(null); // Marketplace task - no assigned child
        marketplaceTask.setPickedByChild(null); // Not picked yet
        marketplaceTask.setActive(true);

        // Setup assigned task (has assigned child)
        assignedTask = new Task();
        assignedTask.setId(200L);
        assignedTask.setTitle("Assigned Task");
        assignedTask.setDescription("Assigned to child");
        assignedTask.setPoints(10);
        assignedTask.setType(TaskType.ONE_TIME);
        assignedTask.setStatus(TaskStatus.APPROVED);
        assignedTask.setCreatedBy(parentUser);
        assignedTask.setAssignedChild(childUser);
        assignedTask.setPickedByChild(null);
        assignedTask.setActive(true);
    }

    @Test
    void pickTask_ShouldSetPickedByChild() {
        // Given
        when(taskRepository.findById(100L)).thenReturn(Optional.of(marketplaceTask));
        when(childRepository.findById(10L)).thenReturn(Optional.of(childUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TaskDTO result = taskService.pickTask(100L, 10L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getTitle()).isEqualTo("Marketplace Task");
        assertThat(result.getPickedByChildId()).isEqualTo(10L);

        verify(taskRepository).findById(100L);
        verify(childRepository).findById(10L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void pickTask_ShouldValidateParentOwnership() {
        // Given
        marketplaceTask.setCreatedBy(otherParentUser); // Task from different family
        when(taskRepository.findById(100L)).thenReturn(Optional.of(marketplaceTask));
        when(childRepository.findById(10L)).thenReturn(Optional.of(childUser));

        // When & Then
        assertThatThrownBy(() -> taskService.pickTask(100L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能认领其他家庭的任务");

        verify(taskRepository).findById(100L);
        verify(childRepository).findById(10L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void pickTask_ShouldNotPickAlreadyPickedTask() {
        // Given
        marketplaceTask.setPickedByChild(otherChildUser); // Already picked by another child
        when(taskRepository.findById(100L)).thenReturn(Optional.of(marketplaceTask));
        when(childRepository.findById(10L)).thenReturn(Optional.of(childUser));

        // When & Then
        assertThatThrownBy(() -> taskService.pickTask(100L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("任务已被其他孩子认领");

        verify(taskRepository).findById(100L);
        verify(childRepository).findById(10L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void unpickTask_ShouldClearPickedByChild() {
        // Given
        marketplaceTask.setPickedByChild(childUser); // Child picked this task
        when(taskRepository.findById(100L)).thenReturn(Optional.of(marketplaceTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        taskService.unpickTask(100L, 10L);

        // Then
        verify(taskRepository).findById(100L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void unpickTask_ShouldValidateOwnership() {
        // Given
        marketplaceTask.setPickedByChild(otherChildUser); // Picked by different child
        when(taskRepository.findById(100L)).thenReturn(Optional.of(marketplaceTask));

        // When & Then
        assertThatThrownBy(() -> taskService.unpickTask(100L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能取消其他孩子认领的任务");

        verify(taskRepository).findById(100L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getMarketplaceTasks_ShouldReturnParentTasksOnly() {
        // Given
        Task otherFamilyTask = new Task();
        otherFamilyTask.setId(300L);
        otherFamilyTask.setTitle("Other Family Task");
        otherFamilyTask.setPoints(20);
        otherFamilyTask.setType(TaskType.REPEATABLE);
        otherFamilyTask.setStatus(TaskStatus.APPROVED);
        otherFamilyTask.setCreatedBy(otherParentUser);
        otherFamilyTask.setAssignedChild(null);
        otherFamilyTask.setPickedByChild(null);
        otherFamilyTask.setActive(true);

        List<Task> marketplaceTasks = Arrays.asList(marketplaceTask);
        when(childRepository.findById(10L)).thenReturn(Optional.of(childUser));
        when(taskRepository.findAvailableMarketplaceTasksByParentId(1L)).thenReturn(marketplaceTasks);

        // When
        List<TaskDTO> result = taskService.getMarketplaceTasks(10L); // Child 10 from parent 1's family

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
        assertThat(result.get(0).getTitle()).isEqualTo("Marketplace Task");

        verify(childRepository).findById(10L);
        verify(taskRepository).findAvailableMarketplaceTasksByParentId(1L);
    }

    @Test
    void getPickedTasks_ShouldReturnChildPickedTasks() {
        // Given
        Task pickedTask1 = new Task();
        pickedTask1.setId(101L);
        pickedTask1.setTitle("Picked Task 1");
        pickedTask1.setPoints(10);
        pickedTask1.setType(TaskType.REPEATABLE);
        pickedTask1.setStatus(TaskStatus.APPROVED);
        pickedTask1.setCreatedBy(parentUser);
        pickedTask1.setAssignedChild(null);
        pickedTask1.setPickedByChild(childUser);
        pickedTask1.setActive(true);

        Task pickedTask2 = new Task();
        pickedTask2.setId(102L);
        pickedTask2.setTitle("Picked Task 2");
        pickedTask2.setPoints(15);
        pickedTask2.setType(TaskType.ONE_TIME);
        pickedTask2.setStatus(TaskStatus.APPROVED);
        pickedTask2.setCreatedBy(parentUser);
        pickedTask2.setAssignedChild(null);
        pickedTask2.setPickedByChild(childUser);
        pickedTask2.setActive(true);

        List<Task> pickedTasks = Arrays.asList(pickedTask1, pickedTask2);
        when(taskRepository.findPickedTasksByChildId(10L)).thenReturn(pickedTasks);

        // When
        List<TaskDTO> result = taskService.getPickedTasks(10L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(101L);
        assertThat(result.get(0).getPickedByChildId()).isEqualTo(10L);
        assertThat(result.get(1).getId()).isEqualTo(102L);
        assertThat(result.get(1).getPickedByChildId()).isEqualTo(10L);

        verify(taskRepository).findPickedTasksByChildId(10L);
    }

    @Test
    void getTasksByChild_ShouldIncludePickedTasks() {
        // Given
        Task pickedTask = new Task();
        pickedTask.setId(101L);
        pickedTask.setTitle("Picked Task");
        pickedTask.setPoints(10);
        pickedTask.setType(TaskType.REPEATABLE);
        pickedTask.setStatus(TaskStatus.APPROVED);
        pickedTask.setCreatedBy(parentUser);
        pickedTask.setAssignedChild(null);
        pickedTask.setPickedByChild(childUser);
        pickedTask.setActive(true);

        List<Task> allTasks = Arrays.asList(assignedTask, pickedTask);
        when(taskRepository.findActiveTasksWithChild(10L)).thenReturn(allTasks);

        // When
        List<TaskDTO> result = taskService.getTasksByChild(10L);

        // Then
        assertThat(result).hasSize(2);
        // Should include assigned task
        assertThat(result).anyMatch(task -> task.getId().equals(200L) && task.getAssignedChildId().equals(10L));
        // Should include picked task
        assertThat(result).anyMatch(task -> task.getId().equals(101L) && task.getPickedByChildId().equals(10L));

        verify(taskRepository).findActiveTasksWithChild(10L);
    }

    @Test
    void DAILY_ONCE_PickedTask_ShouldEnforceLimit() {
        // Given
        Task dailyOnceTask = new Task();
        dailyOnceTask.setId(400L);
        dailyOnceTask.setTitle("Daily Once Marketplace Task");
        dailyOnceTask.setDescription("Daily once task in marketplace");
        dailyOnceTask.setPoints(20);
        dailyOnceTask.setType(TaskType.DAILY_ONCE);
        dailyOnceTask.setStatus(TaskStatus.APPROVED);
        dailyOnceTask.setCreatedBy(parentUser);
        dailyOnceTask.setAssignedChild(null);
        dailyOnceTask.setPickedByChild(childUser);
        dailyOnceTask.setActive(true);

        when(taskRepository.findById(400L)).thenReturn(Optional.of(dailyOnceTask));
        when(childRepository.findById(10L)).thenReturn(Optional.of(childUser));
        when(taskCompletionRepository.existsCompletionToday(
                eq(10L), eq(400L), any(java.time.LocalDateTime.class), any(java.time.LocalDateTime.class)))
                .thenReturn(true); // Already completed today

        // When & Then
        assertThatThrownBy(() -> taskService.completeTask(400L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("该任务每天只能完成一次")
                .hasFieldOrPropertyWithValue("errorCode", "DAILY_LIMIT_EXCEEDED");

        verify(taskRepository).findById(400L);
        verify(childRepository).findById(10L);
        verify(taskCompletionRepository).existsCompletionToday(
                eq(10L), eq(400L), any(java.time.LocalDateTime.class), any(java.time.LocalDateTime.class));
        verify(taskCompletionRepository, never()).save(any());
    }
}
