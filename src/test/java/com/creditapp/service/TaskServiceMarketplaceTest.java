package com.creditapp.service;

import com.creditapp.dto.TaskDTO;
import com.creditapp.entity.Child;
import com.creditapp.entity.JobStatus;
import com.creditapp.entity.Task;
import com.creditapp.entity.TaskJob;
import com.creditapp.entity.TaskType;
import com.creditapp.entity.TaskStatus;
import com.creditapp.entity.User;
import com.creditapp.entity.UserRole;
import com.creditapp.exception.BusinessException;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.repository.ChildRepository;
import com.creditapp.repository.TaskCompletionRepository;
import com.creditapp.repository.TaskJobRepository;
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

    @Mock
    private TaskJobRepository taskJobRepository;

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
        when(taskJobRepository.existsActiveJobByTaskIdAndChildId(100L, 10L)).thenReturn(false);
        when(taskJobRepository.save(any(TaskJob.class))).thenAnswer(invocation -> {
            TaskJob job = invocation.getArgument(0);
            job.setId(1L);
            return job;
        });

        // When
        TaskDTO result = taskService.pickTask(100L, 10L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getTitle()).isEqualTo("Marketplace Task");

        verify(taskRepository).findById(100L);
        verify(childRepository).findById(10L);
        verify(taskJobRepository).existsActiveJobByTaskIdAndChildId(100L, 10L);
        verify(taskJobRepository).save(any(TaskJob.class));
    }

    @Test
    void pickTask_ShouldValidateParentOwnership() {
        // Given
        marketplaceTask.setCreatedBy(otherParentUser);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(marketplaceTask));
        when(childRepository.findById(10L)).thenReturn(Optional.of(childUser));

        // When & Then
        assertThatThrownBy(() -> taskService.pickTask(100L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能认领其他家庭的任务");

        verify(taskRepository).findById(100L);
        verify(childRepository).findById(10L);
        verify(taskJobRepository, never()).save(any(TaskJob.class));
    }

    @Test
    void pickTask_ShouldNotPickAlreadyPickedTask() {
        // Given
        when(taskRepository.findById(100L)).thenReturn(Optional.of(marketplaceTask));
        when(childRepository.findById(10L)).thenReturn(Optional.of(childUser));
        when(taskJobRepository.existsActiveJobByTaskIdAndChildId(100L, 10L)).thenReturn(true); // Already picked

        // When & Then
        assertThatThrownBy(() -> taskService.pickTask(100L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("任务已被其他孩子认领");

        verify(taskRepository).findById(100L);
        verify(childRepository).findById(10L);
        verify(taskJobRepository).existsActiveJobByTaskIdAndChildId(100L, 10L);
        verify(taskJobRepository, never()).save(any(TaskJob.class));
    }

    @Test
    void unpickTask_ShouldClearPickedByChild() {
        // Given
        TaskJob taskJob = TaskJob.builder()
                .id(1L)
                .task(marketplaceTask)
                .child(childUser)
                .status(JobStatus.ASSIGNED)
                .snapshotTitle(marketplaceTask.getTitle())
                .snapshotPoints(marketplaceTask.getPoints())
                .snapshotTaskType(marketplaceTask.getType())
                .build();

        when(taskJobRepository.findByTaskIdAndChildId(100L, 10L)).thenReturn(Optional.of(taskJob));
        when(taskJobRepository.save(any(TaskJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        taskService.unpickTask(100L, 10L);

        // Then
        verify(taskJobRepository).findByTaskIdAndChildId(100L, 10L);
        verify(taskJobRepository).save(any(TaskJob.class));
    }

    @Test
    void unpickTask_ShouldValidateOwnership() {
        // Given - TaskJob doesn't exist for this child (different child picked it)
        when(taskJobRepository.findByTaskIdAndChildId(100L, 10L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.unpickTask(100L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("任务未被认领");

        verify(taskJobRepository).findByTaskIdAndChildId(100L, 10L);
        verify(taskJobRepository, never()).save(any(TaskJob.class));
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
        pickedTask1.setActive(true);

        Task pickedTask2 = new Task();
        pickedTask2.setId(102L);
        pickedTask2.setTitle("Picked Task 2");
        pickedTask2.setPoints(15);
        pickedTask2.setType(TaskType.ONE_TIME);
        pickedTask2.setStatus(TaskStatus.APPROVED);
        pickedTask2.setCreatedBy(parentUser);
        pickedTask2.setActive(true);

        TaskJob job1 = TaskJob.builder()
                .id(1L)
                .task(pickedTask1)
                .child(childUser)
                .status(JobStatus.ASSIGNED)
                .snapshotTitle(pickedTask1.getTitle())
                .snapshotPoints(pickedTask1.getPoints())
                .snapshotTaskType(pickedTask1.getType())
                .build();

        TaskJob job2 = TaskJob.builder()
                .id(2L)
                .task(pickedTask2)
                .child(childUser)
                .status(JobStatus.ASSIGNED)
                .snapshotTitle(pickedTask2.getTitle())
                .snapshotPoints(pickedTask2.getPoints())
                .snapshotTaskType(pickedTask2.getType())
                .build();

        List<TaskJob> pickedJobs = Arrays.asList(job1, job2);
        when(taskJobRepository.findPickedJobsByChildId(10L)).thenReturn(pickedJobs);

        // When
        List<TaskDTO> result = taskService.getPickedTasks(10L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(101L);
        assertThat(result.get(1).getId()).isEqualTo(102L);

        verify(taskJobRepository).findPickedJobsByChildId(10L);
    }

    @Test
    void getTasksByChild_ShouldIncludePickedTasks() {
        // Given
        TaskJob job1 = TaskJob.builder()
                .id(1L)
                .task(assignedTask)
                .child(childUser)
                .status(JobStatus.ASSIGNED)
                .snapshotTitle(assignedTask.getTitle())
                .snapshotPoints(assignedTask.getPoints())
                .snapshotTaskType(assignedTask.getType())
                .build();

        Task pickedTask = new Task();
        pickedTask.setId(101L);
        pickedTask.setTitle("Picked Task");
        pickedTask.setPoints(10);
        pickedTask.setType(TaskType.REPEATABLE);
        pickedTask.setStatus(TaskStatus.APPROVED);
        pickedTask.setCreatedBy(parentUser);
        pickedTask.setActive(true);

        TaskJob job2 = TaskJob.builder()
                .id(2L)
                .task(pickedTask)
                .child(childUser)
                .status(JobStatus.ASSIGNED)
                .snapshotTitle(pickedTask.getTitle())
                .snapshotPoints(pickedTask.getPoints())
                .snapshotTaskType(pickedTask.getType())
                .build();

        List<TaskJob> allJobs = Arrays.asList(job1, job2);
        when(taskJobRepository.findActiveJobsByChildId(10L)).thenReturn(allJobs);

        // When
        List<TaskDTO> result = taskService.getTasksByChild(10L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).anyMatch(task -> task.getId().equals(200L));
        assertThat(result).anyMatch(task -> task.getId().equals(101L));

        verify(taskJobRepository).findActiveJobsByChildId(10L);
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
        dailyOnceTask.setActive(true);

        TaskJob taskJob = TaskJob.builder()
                .id(1L)
                .task(dailyOnceTask)
                .child(childUser)
                .status(JobStatus.ASSIGNED)
                .snapshotTitle(dailyOnceTask.getTitle())
                .snapshotPoints(dailyOnceTask.getPoints())
                .snapshotTaskType(dailyOnceTask.getType())
                .build();

        when(taskRepository.findById(400L)).thenReturn(Optional.of(dailyOnceTask));
        when(childRepository.findById(10L)).thenReturn(Optional.of(childUser));
        when(taskJobRepository.findByTaskIdAndChildId(400L, 10L)).thenReturn(Optional.of(taskJob));
        when(taskCompletionRepository.existsCompletionToday(
                eq(10L), eq(400L), any(java.time.LocalDateTime.class), any(java.time.LocalDateTime.class)))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> taskService.completeTask(400L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("该任务每天只能完成一次")
                .hasFieldOrPropertyWithValue("errorCode", "DAILY_LIMIT_EXCEEDED");

        verify(taskRepository).findById(400L);
        verify(childRepository).findById(10L);
        verify(taskJobRepository).findByTaskIdAndChildId(400L, 10L);
        verify(taskCompletionRepository).existsCompletionToday(
                eq(10L), eq(400L), any(java.time.LocalDateTime.class), any(java.time.LocalDateTime.class));
        verify(taskCompletionRepository, never()).save(any());
    }
}
