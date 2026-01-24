package com.creditapp.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test to verify the complete authentication flow
 * This test simulates the actual browser behavior
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCompleteAuthenticationFlow() throws Exception {
        // Step 1: Access login page
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

        // Step 2: Submit login form
        var result = mockMvc.perform(post("/login")
                        .param("username", "parent")
                        .param("password", "parent123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        // Step 3: Extract session cookie from login response
        var sessionCookie = result.getResponse().getCookie("JSESSIONID");
        
        if (sessionCookie != null) {
            // Step 4: Follow redirect to dashboard with session cookie
            mockMvc.perform(get("/dashboard")
                            .cookie(sessionCookie))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attributeExists("role"));
        } else {
            throw new AssertionError("No JSESSIONID cookie set after login");
        }
    }

    @Test
    void testLoginFailure() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "wronguser")
                        .param("password", "wrongpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    void testSessionPersistence() throws Exception {
        // Login
        var loginResult = mockMvc.perform(post("/login")
                        .param("username", "parent")
                        .param("password", "parent123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        var sessionCookie = loginResult.getResponse().getCookie("JSESSIONID");
        
        if (sessionCookie != null) {
            // Access dashboard multiple times with same session
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(get("/dashboard")
                                .cookie(sessionCookie))
                        .andExpect(status().isOk())
                        .andExpect(view().name("dashboard"));
            }
        } else {
            throw new AssertionError("Session not established");
        }
    }

    @Test
    void testLogoutFlow() throws Exception {
        // Login first
        var loginResult = mockMvc.perform(post("/login")
                        .param("username", "parent")
                        .param("password", "parent123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        var sessionCookie = loginResult.getResponse().getCookie("JSESSIONID");
        
        if (sessionCookie != null) {
            // Logout
            mockMvc.perform(post("/logout")
                            .cookie(sessionCookie))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?logout"));

            // Try to access dashboard after logout
            mockMvc.perform(get("/dashboard")
                            .cookie(sessionCookie))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }
}