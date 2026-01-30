package com.creditapp.controller;

import com.creditapp.dto.ChildDTO;
import com.creditapp.dto.TaskDTO;
import com.creditapp.entity.TaskType;
import com.creditapp.entity.User;
import com.creditapp.entity.UserRole;
import com.creditapp.service.TaskService;
import com.creditapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ViewControllerTaskTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private TaskService taskService;

    @Test
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void testParentTasksEndpointReturnsViewWithTasks() throws Exception {
        // Mock user data
        User parentUser = new User();
        parentUser.setId(1L);
        parentUser.setUsername("parent");
        parentUser.setRole(UserRole.PARENT);

        // Mock children data
        ChildDTO child1 = ChildDTO.builder()
                .id(1L)
                .username("child1")
                .points(100)
                .build();

        // Mock tasks data
        TaskDTO task1 = TaskDTO.builder()
                .id(1L)
                .title("完成作业")
                .description("按时完成学校作业")
                .points(10)
                .type(TaskType.DAILY_ONCE)
                .assignedChildId(1L)
                .assignedChildName("child1")
                .build();

        TaskDTO task2 = TaskDTO.builder()
                .id(2L)
                .title("打扫房间")
                .description("整理自己的房间")
                .points(5)
                .type(TaskType.REPEATABLE)
                .assignedChildId(1L)
                .assignedChildName("child1")
                .build();

        when(userService.findByUsername("parent")).thenReturn(java.util.Optional.of(parentUser));
        when(userService.getChildrenByParentId(1L)).thenReturn(Arrays.asList(child1));
        when(taskService.getTasksByParent(1L)).thenReturn(Arrays.asList(task1, task2));

        mockMvc.perform(get("/parent/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("parent/tasks"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attributeExists("children"))
                .andExpect(model().attribute("tasks", Arrays.asList(task1, task2)))
                .andExpect(model().attribute("children", Arrays.asList(child1)));
    }

    @Test
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void testCreateTaskWithValidDataRedirects() throws Exception {
        // Mock user data
        User parentUser = new User();
        parentUser.setId(1L);
        parentUser.setUsername("parent");
        parentUser.setRole(UserRole.PARENT);

        when(userService.findByUsername("parent")).thenReturn(java.util.Optional.of(parentUser));

        mockMvc.perform(post("/parent/tasks")
                        .param("title", "新任务")
                        .param("description", "任务描述")
                        .param("points", "10")
                        .param("type", "DAILY_ONCE")
                        .param("childId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/parent/tasks?success=true"));
    }

    @Test
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void testCreateTaskWithInvalidTaskTypeReturnsError() throws Exception {
        mockMvc.perform(post("/parent/tasks")
                        .param("title", "新任务")
                        .param("description", "任务描述")
                        .param("points", "10")
                        .param("type", "INVALID_TYPE")  // Invalid type
                        .param("childId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/parent/tasks"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void testCreateTaskWithMissingRequiredFieldsReturnsError() throws Exception {
        // Test missing title - Spring will throw MissingServletRequestParameterException before our method is called
        mockMvc.perform(post("/parent/tasks")
                        .param("description", "任务描述")
                        .param("points", "10")
                        .param("type", "DAILY_ONCE")
                        .param("childId", "1"))
                .andExpect(status().isInternalServerError()); // 500 error from global exception handler

        // Test missing points - Spring will throw MissingServletRequestParameterException before our method is called
        mockMvc.perform(post("/parent/tasks")
                        .param("title", "新任务")
                        .param("description", "任务描述")
                        .param("type", "DAILY_ONCE")
                        .param("childId", "1"))
                .andExpect(status().isInternalServerError()); // 500 error from global exception handler
    }

    @Test
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void testCreateTaskSuccessMessageInFlashAttribute() throws Exception {
        // Mock user data
        User parentUser = new User();
        parentUser.setId(1L);
        parentUser.setUsername("parent");
        parentUser.setRole(UserRole.PARENT);

        when(userService.findByUsername("parent")).thenReturn(java.util.Optional.of(parentUser));

        mockMvc.perform(post("/parent/tasks")
                        .param("title", "新任务")
                        .param("description", "任务描述")
                        .param("points", "10")
                        .param("type", "DAILY_ONCE")
                        .param("childId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/parent/tasks?success=true"))
                .andExpect(flash().attribute("success", "任务创建成功！"));
    }

    @Test
    void testTaskEndpointsRequireAuthentication() throws Exception {
        // Test GET /parent/tasks without authentication
        mockMvc.perform(get("/parent/tasks"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        // Test POST /parent/tasks without authentication
        mockMvc.perform(post("/parent/tasks"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "child", roles = {"CHILD"})
    void testParentTaskEndpointsRequireParentRole() throws Exception {
        // Child should not be able to access parent task endpoints
        mockMvc.perform(get("/parent/tasks"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/parent/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void testCreateTaskErrorHandlingForServiceExceptions() throws Exception {
        // Mock user data
        User parentUser = new User();
        parentUser.setId(1L);
        parentUser.setUsername("parent");
        parentUser.setRole(UserRole.PARENT);

        when(userService.findByUsername("parent")).thenReturn(java.util.Optional.of(parentUser));
        
        // Mock service to throw exception
        when(taskService.createTask(any(), anyLong())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/parent/tasks")
                        .param("title", "新任务")
                        .param("description", "任务描述")
                        .param("points", "10")
                        .param("type", "DAILY_ONCE")
                        .param("childId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/parent/tasks"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "创建任务失败: Database error"));
    }

    @Test
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void testParentTasksWithNoTasksReturnsEmptyList() throws Exception {
        // Mock user data
        User parentUser = new User();
        parentUser.setId(1L);
        parentUser.setUsername("parent");
        parentUser.setRole(UserRole.PARENT);

        // Mock children data
        ChildDTO child1 = ChildDTO.builder()
                .id(1L)
                .username("child1")
                .points(100)
                .build();

        when(userService.findByUsername("parent")).thenReturn(java.util.Optional.of(parentUser));
        when(userService.getChildrenByParentId(1L)).thenReturn(Arrays.asList(child1));
        when(taskService.getTasksByParent(1L)).thenReturn(Arrays.asList()); // Empty list

        mockMvc.perform(get("/parent/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("parent/tasks"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attribute("tasks", Arrays.asList()))
                .andExpect(model().attributeExists("children"));
    }
}