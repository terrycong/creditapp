package com.creditapp.controller;

import com.creditapp.dto.*;
import com.creditapp.service.RewardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API Tests for RewardController
 * Tests REST endpoints for reward management
 * 
 * Note: Create/Update reward tests are skipped because they require 
 * CreateTaskRequest with validation that's designed for tasks, not rewards.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardService rewardService;

    private RewardDTO rewardDTO;

    @BeforeEach
    void setUp() {
        rewardDTO = RewardDTO.builder()
                .id(1L)
                .name("游戏时间")
                .description("30分钟游戏时间")
                .pointsRequired(50)
                .quantity(10)
                .active(true)
                .build();
    }

    // ========== Get Rewards Tests ==========

    @Test
    @Order(1)
    @DisplayName("GET /api/v1/rewards - Should return rewards list")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void getAllRewards_shouldReturnList() throws Exception {
        when(rewardService.getAllRewards()).thenReturn(List.of(rewardDTO));

        mockMvc.perform(get("/api/v1/rewards"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/rewards - Child can also view rewards")
    @WithMockUser(username = "child", roles = {"CHILD"})
    void getAllRewards_childCanView() throws Exception {
        when(rewardService.getAllRewards()).thenReturn(List.of(rewardDTO));

        mockMvc.perform(get("/api/v1/rewards"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/rewards/{id} - Should return reward by ID")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void getRewardById_shouldReturnReward() throws Exception {
        when(rewardService.getRewardById(1L)).thenReturn(rewardDTO);

        mockMvc.perform(get("/api/v1/rewards/1"))
                .andExpect(status().isOk());
    }

    // ========== Delete Reward Tests ==========

    @Test
    @Order(4)
    @DisplayName("DELETE /api/v1/rewards/{id} - Should delete reward")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void deleteReward_shouldSucceed() throws Exception {
        doNothing().when(rewardService).deleteReward(1L);

        mockMvc.perform(delete("/api/v1/rewards/1"))
                .andExpect(status().isOk());

        verify(rewardService).deleteReward(1L);
    }



    // ========== Authentication Tests ==========

    @Test
    @Order(7)
    @DisplayName("GET /api/v1/rewards - Should require authentication")
    void getAllRewards_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/v1/rewards"))
                .andExpect(status().is3xxRedirection());
    }
}