package com.creditapp.controller;

import com.creditapp.dto.*;
import com.creditapp.entity.User;
import com.creditapp.entity.UserRole;
import com.creditapp.service.LotteryService;
import com.creditapp.service.UserService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API Tests for LotteryController
 * Tests REST endpoints for lottery management
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LotteryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LotteryService lotteryService;

    @MockBean
    private UserService userService;

    private CreateLotteryThemeRequest themeRequest;
    private LotteryThemeDTO themeDTO;
    private User parentUser;
    private User childUser;

    @BeforeEach
    void setUp() {
        parentUser = new User();
        parentUser.setId(1L);
        parentUser.setUsername("parent");
        parentUser.setRole(UserRole.PARENT);

        childUser = new User();
        childUser.setId(2L);
        childUser.setUsername("child");
        childUser.setRole(UserRole.CHILD);

        themeRequest = new CreateLotteryThemeRequest();
        themeRequest.setName("周末抽奖");
        themeRequest.setDescription("周末家庭活动抽奖");
        themeRequest.setPointsPerDraw(10);

        themeDTO = LotteryThemeDTO.builder()
                .id(1L)
                .name("周末抽奖")
                .description("周末家庭活动抽奖")
                .pointsPerDraw(10)
                .active(true)
                .build();
    }

    // ========== Theme Management Tests ==========

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/lottery/themes - Should create theme")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void createTheme_shouldSucceed() throws Exception {
        when(userService.findByUsername("parent")).thenReturn(Optional.of(parentUser));
        when(lotteryService.createTheme(any(CreateLotteryThemeRequest.class), eq(1L))).thenReturn(themeDTO);

        mockMvc.perform(post("/api/v1/lottery/themes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(themeRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/lottery/themes - Should return themes")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void getMyThemes_shouldReturnThemes() throws Exception {
        when(userService.findByUsername("parent")).thenReturn(Optional.of(parentUser));
        when(lotteryService.getThemesByParent(1L)).thenReturn(List.of(themeDTO));

        mockMvc.perform(get("/api/v1/lottery/themes"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/lottery/themes/{themeId} - Should return theme")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void getTheme_shouldReturnTheme() throws Exception {
        when(lotteryService.getThemeById(1L)).thenReturn(themeDTO);

        mockMvc.perform(get("/api/v1/lottery/themes/1"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @DisplayName("PUT /api/v1/lottery/themes/{themeId} - Should update theme")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void updateTheme_shouldSucceed() throws Exception {
        when(lotteryService.updateTheme(eq(1L), any(CreateLotteryThemeRequest.class))).thenReturn(themeDTO);

        mockMvc.perform(put("/api/v1/lottery/themes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(themeRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /api/v1/lottery/themes/{themeId} - Should delete theme")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void deleteTheme_shouldSucceed() throws Exception {
        doNothing().when(lotteryService).deleteTheme(1L);

        mockMvc.perform(delete("/api/v1/lottery/themes/1"))
                .andExpect(status().isOk());

        verify(lotteryService).deleteTheme(1L);
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/v1/lottery/themes/{themeId}/toggle - Should toggle theme")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void toggleTheme_shouldSucceed() throws Exception {
        themeDTO.setActive(false);
        when(lotteryService.toggleThemeActive(1L)).thenReturn(themeDTO);

        mockMvc.perform(post("/api/v1/lottery/themes/1/toggle"))
                .andExpect(status().isOk());
    }

    // ========== Prize Management Tests ==========

    @Test
    @Order(7)
    @DisplayName("POST /api/v1/lottery/prizes - Should create prize")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void createPrize_shouldSucceed() throws Exception {
        CreateLotteryPrizeRequest prizeRequest = new CreateLotteryPrizeRequest();
        prizeRequest.setName("冰淇淋");
        prizeRequest.setProbability(30);

        LotteryPrizeDTO prizeDTO = LotteryPrizeDTO.builder()
                .id(1L)
                .name("冰淇淋")
                .probability(30)
                .build();

        when(lotteryService.createPrize(any(CreateLotteryPrizeRequest.class))).thenReturn(prizeDTO);

        mockMvc.perform(post("/api/v1/lottery/prizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prizeRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    @DisplayName("DELETE /api/v1/lottery/prizes/{prizeId} - Should delete prize")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void deletePrize_shouldSucceed() throws Exception {
        doNothing().when(lotteryService).deletePrize(1L);

        mockMvc.perform(delete("/api/v1/lottery/prizes/1"))
                .andExpect(status().isOk());

        verify(lotteryService).deletePrize(1L);
    }

    @Test
    @Order(9)
    @DisplayName("GET /api/v1/lottery/themes/{themeId}/prizes - Should return prizes")
    @WithMockUser(username = "parent", roles = {"PARENT"})
    void getPrizesByTheme_shouldReturnPrizes() throws Exception {
        LotteryPrizeDTO prizeDTO = LotteryPrizeDTO.builder()
                .id(1L)
                .name("冰淇淋")
                .build();

        when(lotteryService.getPrizesByTheme(1L)).thenReturn(List.of(prizeDTO));

        mockMvc.perform(get("/api/v1/lottery/themes/1/prizes"))
                .andExpect(status().isOk());
    }

    // ========== Draw Tests ==========

    @Test
    @Order(10)
    @DisplayName("GET /api/v1/lottery/themes/active - Should return active themes")
    @WithMockUser(username = "child", roles = {"CHILD"})
    void getActiveThemes_shouldReturnActiveThemes() throws Exception {
        when(lotteryService.getAllActiveThemesWithPrizes()).thenReturn(List.of(themeDTO));

        mockMvc.perform(get("/api/v1/lottery/themes/active"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    @DisplayName("POST /api/v1/lottery/themes/{themeId}/draw - Should perform draw")
    @WithMockUser(username = "child", roles = {"CHILD"})
    void draw_shouldSucceed() throws Exception {
        LotteryDrawDTO drawDTO = LotteryDrawDTO.builder()
                .id(1L)
                .themeId(1L)
                .childId(2L)
                .themeName("周末抽奖")
                .pointsCost(10)
                .build();

        when(userService.findByUsername("child")).thenReturn(Optional.of(childUser));
        when(lotteryService.draw(1L, 2L)).thenReturn(drawDTO);

        mockMvc.perform(post("/api/v1/lottery/themes/1/draw"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(12)
    @DisplayName("GET /api/v1/lottery/history - Should return draw history")
    @WithMockUser(username = "child", roles = {"CHILD"})
    void getDrawHistory_shouldReturnHistory() throws Exception {
        LotteryDrawDTO drawDTO = LotteryDrawDTO.builder()
                .id(1L)
                .themeId(1L)
                .build();

        when(userService.findByUsername("child")).thenReturn(Optional.of(childUser));
        when(lotteryService.getDrawHistory(2L)).thenReturn(List.of(drawDTO));

        mockMvc.perform(get("/api/v1/lottery/history"))
                .andExpect(status().isOk());
    }

    // ========== Authentication Tests ==========

    @Test
    @Order(20)
    @DisplayName("GET /api/v1/lottery/themes - Should require authentication")
    void getThemes_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/v1/lottery/themes"))
                .andExpect(status().is3xxRedirection());
    }
}