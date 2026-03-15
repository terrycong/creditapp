package com.creditapp.controller;

import com.creditapp.dto.*;
import com.creditapp.entity.TaskType;
import com.creditapp.entity.TaskStatus;
import com.creditapp.entity.CompletionStatus;
import com.creditapp.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API Tests for TaskController
 * Tests REST endpoints for task management
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private CreateTaskRequest createTaskRequest;
    private TaskDTO taskDTO;

    @BeforeEach
    void setUp() {
        createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTitle("Test Task");
        createTaskRequest.setDescription("Test Description");
        createTaskRequest.setPoints(10);
        createTaskRequest.setType(TaskType.DAILY_ONCE);

        taskDTO = TaskDTO.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .points(10)
                .type(TaskType.DAILY_ONCE)
                .status(TaskStatus.APPROVED)
                .active(true)
                .build();
    }

    // ========== Create Task Tests ==========

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/tasks - Should create task with authentication")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void createTask_withAuth_shouldProcess() throws Exception {
        when(taskService.createTask(any(CreateTaskRequest.class), anyLong())).thenReturn(taskDTO);

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/tasks - Should redirect without authentication")
    void createTask_withoutAuth_shouldRedirect() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest)))
                .andExpect(status().is3xxRedirection());
    }

    // ========== Get Tasks Tests ==========

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/tasks - Should return tasks list")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void getAllTasks_shouldReturnList() throws Exception {
        when(taskService.getAllTasks()).thenReturn(List.of(taskDTO));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/tasks/{id} - Should return task by ID")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void getTaskById_shouldReturnTask() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(taskDTO);

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk());
    }

    // ========== Update Task Tests ==========

    @Test
    @Order(5)
    @DisplayName("PUT /api/v1/tasks/{id} - Should update task")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void updateTask_shouldSucceed() throws Exception {
        taskDTO.setTitle("Updated Task");
        when(taskService.updateTask(eq(1L), any(CreateTaskRequest.class))).thenReturn(taskDTO);

        mockMvc.perform(put("/api/v1/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskRequest)))
                .andExpect(status().isOk());
    }

    // ========== Delete Task Tests ==========

    @Test
    @Order(6)
    @DisplayName("DELETE /api/v1/tasks/{id} - Should delete task")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void deleteTask_shouldSucceed() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/v1/tasks/1"))
                .andExpect(status().isOk());

        verify(taskService).deleteTask(1L);
    }

    // ========== Complete Task Tests ==========

    @Test
    @Order(7)
    @DisplayName("POST /api/v1/tasks/{id}/complete - Should complete task")
    @WithMockUser(username = "child", roles = {"CHILD"})
    void completeTask_shouldSucceed() throws Exception {
        TaskCompletionDTO completion = TaskCompletionDTO.builder()
                .id(1L)
                .taskId(1L)
                .taskTitle("Test Task")
                .status(CompletionStatus.PENDING)
                .build();

        when(taskService.completeTask(eq(1L), anyLong())).thenReturn(completion);

        mockMvc.perform(post("/api/v1/tasks/1/complete"))
                .andExpect(status().isOk());
    }

    // ========== Approval Tests ==========

    @Test
    @Order(8)
    @DisplayName("POST /api/v1/tasks/approvals/{completionId} - Should approve completion")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void approveCompletion_shouldSucceed() throws Exception {
        TaskCompletionDTO completion = TaskCompletionDTO.builder()
                .id(1L)
                .taskId(1L)
                .status(CompletionStatus.APPROVED)
                .build();

        when(taskService.approveCompletion(1L)).thenReturn(completion);

        mockMvc.perform(post("/api/v1/tasks/approvals/1"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    @DisplayName("POST /api/v1/tasks/rejections/{completionId} - Should reject completion")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void rejectCompletion_shouldSucceed() throws Exception {
        TaskCompletionDTO completion = TaskCompletionDTO.builder()
                .id(1L)
                .taskId(1L)
                .status(CompletionStatus.REJECTED)
                .build();

        when(taskService.rejectCompletion(1L)).thenReturn(completion);

        mockMvc.perform(post("/api/v1/tasks/rejections/1"))
                .andExpect(status().isOk());
    }

    // ========== Authentication Tests ==========

    @Test
    @Order(10)
    @DisplayName("GET /api/v1/tasks - Should require authentication")
    void getAllTasks_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().is3xxRedirection());
    }
}