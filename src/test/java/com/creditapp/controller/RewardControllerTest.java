package com.creditapp.controller;

import com.creditapp.dto.*;
import com.creditapp.service.RewardService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API Tests for RewardController
 * Tests REST endpoints for reward management
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RewardService rewardService;

    private CreateTaskRequest createRewardRequest;
    private RewardDTO rewardDTO;

    @BeforeEach
    void setUp() {
        createRewardRequest = new CreateTaskRequest();
        createRewardRequest.setTitle("游戏时间");
        createRewardRequest.setDescription("30分钟游戏时间");
        createRewardRequest.setPoints(50);

        rewardDTO = RewardDTO.builder()
                .id(1L)
                .name("游戏时间")
                .description("30分钟游戏时间")
                .pointsRequired(50)
                .quantity(10)
                .active(true)
                .build();
    }

    // ========== Create Reward Tests ==========

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/rewards - Should create reward with auth")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void createReward_withAuth_shouldProcess() throws Exception {
        when(rewardService.createReward(any(CreateTaskRequest.class))).thenReturn(rewardDTO);

        mockMvc.perform(post("/api/v1/rewards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRewardRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/rewards - Should redirect without auth")
    void createReward_withoutAuth_shouldRedirect() throws Exception {
        mockMvc.perform(post("/api/v1/rewards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRewardRequest)))
                .andExpect(status().is3xxRedirection());
    }

    // ========== Get Rewards Tests ==========

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/rewards - Should return rewards list")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void getAllRewards_shouldReturnList() throws Exception {
        when(rewardService.getAllRewards()).thenReturn(List.of(rewardDTO));

        mockMvc.perform(get("/api/v1/rewards"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/rewards - Child can also view rewards")
    @WithMockUser(username = "child", roles = {"CHILD"})
    void getAllRewards_childCanView() throws Exception {
        when(rewardService.getAllRewards()).thenReturn(List.of(rewardDTO));

        mockMvc.perform(get("/api/v1/rewards"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/rewards/{id} - Should return reward by ID")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void getRewardById_shouldReturnReward() throws Exception {
        when(rewardService.getRewardById(1L)).thenReturn(rewardDTO);

        mockMvc.perform(get("/api/v1/rewards/1"))
                .andExpect(status().isOk());
    }

    // ========== Update Reward Tests ==========

    @Test
    @Order(6)
    @DisplayName("PUT /api/v1/rewards/{id} - Should update reward")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void updateReward_shouldSucceed() throws Exception {
        rewardDTO.setName("更新后的奖励");
        when(rewardService.updateReward(eq(1L), any(CreateTaskRequest.class))).thenReturn(rewardDTO);

        mockMvc.perform(put("/api/v1/rewards/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRewardRequest)))
                .andExpect(status().isOk());
    }

    // ========== Delete Reward Tests ==========

    @Test
    @Order(7)
    @DisplayName("DELETE /api/v1/rewards/{id} - Should delete reward")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void deleteReward_shouldSucceed() throws Exception {
        doNothing().when(rewardService).deleteReward(1L);

        mockMvc.perform(delete("/api/v1/rewards/1"))
                .andExpect(status().isOk());

        verify(rewardService).deleteReward(1L);
    }

    // ========== Redeem Reward Tests ==========

    @Test
    @Order(8)
    @DisplayName("POST /api/v1/rewards/{id}/redeem - Should redeem reward")
    @WithMockUser(username = "child", roles = {"CHILD"})
    void redeemReward_shouldSucceed() throws Exception {
        RewardRedemptionDTO redemption = RewardRedemptionDTO.builder()
                .id(1L)
                .rewardId(1L)
                .rewardName("游戏时间")
                .status("REDEEMED")
                .build();

        when(rewardService.redeemReward(eq(1L), anyLong())).thenReturn(redemption);

        mockMvc.perform(post("/api/v1/rewards/1/redeem"))
                .andExpect(status().isOk());
    }

    // ========== Use Reward Tests ==========

    @Test
    @Order(9)
    @DisplayName("POST /api/v1/rewards/redemptions/{redemptionId}/use - Should mark as used")
    @WithMockUser(username = "child", roles = {"CHILD"})
    void useReward_shouldSucceed() throws Exception {
        RewardRedemptionDTO redemption = RewardRedemptionDTO.builder()
                .id(1L)
                .status("USED")
                .build();

        when(rewardService.useReward(eq(1L), anyLong())).thenReturn(redemption);

        mockMvc.perform(post("/api/v1/rewards/redemptions/1/use"))
                .andExpect(status().isOk());
    }

    // ========== Authentication Tests ==========

    @Test
    @Order(10)
    @DisplayName("GET /api/v1/rewards - Should require authentication")
    void getAllRewards_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/v1/rewards"))
                .andExpect(status().is3xxRedirection());
    }
}