package com.creditapp.security;

import com.creditapp.entity.User;
import com.creditapp.entity.UserRole;
import com.creditapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testLoginPageAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testSuccessfulLoginRedirect() throws Exception {
        // Mock user data
        User parentUser = new User();
        parentUser.setId(1L);
        parentUser.setUsername("parent");
        parentUser.setPassword("$2a$10$BFeE1qUtBvthU1sKwEc.tOIzuFOw1D635dDscfC2cv1HcP1/Co0Su"); // parent123
        parentUser.setRole(UserRole.PARENT);
        parentUser.setPoints(0);

        when(userService.findByUsername(anyString())).thenReturn(Optional.of(parentUser));

        // Test login POST - CSRF is disabled in SecurityConfig
        mockMvc.perform(post("/login")
                        .param("username", "parent")
                        .param("password", "parent123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void testDashboardAccessRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void testDashboardAccessWithAuthentication() throws Exception {
        // Mock user data for the controller
        User parentUser = new User();
        parentUser.setId(1L);
        parentUser.setUsername("parent");
        parentUser.setPassword("$2a$10$BFeE1qUtBvthU1sKwEc.tOIzuFOw1D635dDscfC2cv1HcP1/Co0Su");
        parentUser.setRole(UserRole.PARENT);
        parentUser.setPoints(1000);

        when(userService.findByUsername("parent")).thenReturn(Optional.of(parentUser));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("role"));
    }

    @Test
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void testRoleAttributeInDashboard() throws Exception {
        // Mock user data for the controller
        User parentUser = new User();
        parentUser.setId(1L);
        parentUser.setUsername("parent");
        parentUser.setPassword("$2a$10$BFeE1qUtBvthU1sKwEc.tOIzuFOw1D635dDscfC2cv1HcP1/Co0Su");
        parentUser.setRole(UserRole.PARENT);
        parentUser.setPoints(1000);

        when(userService.findByUsername("parent")).thenReturn(Optional.of(parentUser));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("role", "PARENT"));
    }
}