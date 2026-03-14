package com.creditapp.controller;

import com.creditapp.dto.*;
import com.creditapp.service.LotteryService;
import com.creditapp.service.UserService;
import com.creditapp.entity.User;
import com.creditapp.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/lottery")
@RequiredArgsConstructor
public class LotteryController {

    private final LotteryService lotteryService;
    private final UserService userService;

    // ========== 抽奖主题管理（家长端） ==========

    /**
     * 创建抽奖主题
     */
    @PostMapping("/themes")
    public ResponseEntity<ApiResponse<LotteryThemeDTO>> createTheme(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateLotteryThemeRequest request) {
        log.info("Creating lottery theme: name={}", request.getName());
        
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        LotteryThemeDTO theme = lotteryService.createTheme(request, user.getId());
        return ResponseEntity.ok(ApiResponse.success("抽奖主题创建成功", theme));
    }

    /**
     * 获取家长创建的所有抽奖主题
     */
    @GetMapping("/themes")
    public ResponseEntity<ApiResponse<List<LotteryThemeDTO>>> getMyThemes(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        List<LotteryThemeDTO> themes = lotteryService.getThemesByParent(user.getId());
        return ResponseEntity.ok(ApiResponse.success("获取抽奖主题列表成功", themes));
    }

    /**
     * 获取抽奖主题详情
     */
    @GetMapping("/themes/{themeId}")
    public ResponseEntity<ApiResponse<LotteryThemeDTO>> getTheme(
            @PathVariable Long themeId) {
        LotteryThemeDTO theme = lotteryService.getThemeById(themeId);
        return ResponseEntity.ok(ApiResponse.success("获取抽奖主题详情成功", theme));
    }

    /**
     * 更新抽奖主题
     */
    @PutMapping("/themes/{themeId}")
    public ResponseEntity<ApiResponse<LotteryThemeDTO>> updateTheme(
            @PathVariable Long themeId,
            @RequestBody CreateLotteryThemeRequest request) {
        LotteryThemeDTO theme = lotteryService.updateTheme(themeId, request);
        return ResponseEntity.ok(ApiResponse.success("抽奖主题更新成功", theme));
    }

    /**
     * 删除抽奖主题
     */
    @DeleteMapping("/themes/{themeId}")
    public ResponseEntity<ApiResponse<Void>> deleteTheme(@PathVariable Long themeId) {
        lotteryService.deleteTheme(themeId);
        return ResponseEntity.ok(ApiResponse.success("抽奖主题删除成功", null));
    }

    /**
     * 启用/禁用抽奖主题
     */
    @PostMapping("/themes/{themeId}/toggle")
    public ResponseEntity<ApiResponse<LotteryThemeDTO>> toggleTheme(@PathVariable Long themeId) {
        LotteryThemeDTO theme = lotteryService.toggleThemeActive(themeId);
        return ResponseEntity.ok(ApiResponse.success("抽奖主题状态已更新", theme));
    }

    // ========== 奖品管理（家长端） ==========

    /**
     * 创建奖品
     */
    @PostMapping("/prizes")
    public ResponseEntity<ApiResponse<LotteryPrizeDTO>> createPrize(
            @RequestBody CreateLotteryPrizeRequest request) {
        log.info("Creating lottery prize: name={}", request.getName());
        LotteryPrizeDTO prize = lotteryService.createPrize(request);
        return ResponseEntity.ok(ApiResponse.success("奖品创建成功", prize));
    }

    /**
     * 更新奖品
     */
    @PutMapping("/prizes/{prizeId}")
    public ResponseEntity<ApiResponse<LotteryPrizeDTO>> updatePrize(
            @PathVariable Long prizeId,
            @RequestBody CreateLotteryPrizeRequest request) {
        LotteryPrizeDTO prize = lotteryService.updatePrize(prizeId, request);
        return ResponseEntity.ok(ApiResponse.success("奖品更新成功", prize));
    }

    /**
     * 删除奖品
     */
    @DeleteMapping("/prizes/{prizeId}")
    public ResponseEntity<ApiResponse<Void>> deletePrize(@PathVariable Long prizeId) {
        lotteryService.deletePrize(prizeId);
        return ResponseEntity.ok(ApiResponse.success("奖品删除成功", null));
    }

    /**
     * 获取抽奖主题下的所有奖品
     */
    @GetMapping("/themes/{themeId}/prizes")
    public ResponseEntity<ApiResponse<List<LotteryPrizeDTO>>> getPrizesByTheme(
            @PathVariable Long themeId) {
        List<LotteryPrizeDTO> prizes = lotteryService.getPrizesByTheme(themeId);
        return ResponseEntity.ok(ApiResponse.success("获取奖品列表成功", prizes));
    }

    // ========== 抽奖功能（小孩端） ==========

    /**
     * 获取所有启用的抽奖主题（小孩查看）
     */
    @GetMapping("/themes/active")
    public ResponseEntity<ApiResponse<List<LotteryThemeDTO>>> getActiveThemes() {
        List<LotteryThemeDTO> themes = lotteryService.getAllActiveThemesWithPrizes();
        return ResponseEntity.ok(ApiResponse.success("获取抽奖主题列表成功", themes));
    }

    /**
     * 执行抽奖
     */
    @PostMapping("/themes/{themeId}/draw")
    public ResponseEntity<ApiResponse<LotteryDrawDTO>> draw(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long themeId) {
        log.info("Performing lottery draw: themeId={}", themeId);
        
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        LotteryDrawDTO result = lotteryService.draw(themeId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("抽奖成功", result));
    }

    /**
     * 获取小孩的抽奖历史记录
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<LotteryDrawDTO>>> getDrawHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        List<LotteryDrawDTO> history = lotteryService.getDrawHistory(user.getId());
        return ResponseEntity.ok(ApiResponse.success("获取抽奖历史成功", history));
    }

    /**
     * 获取指定时间范围内的抽奖历史
     */
    @GetMapping("/history/range")
    public ResponseEntity<ApiResponse<List<LotteryDrawDTO>>> getDrawHistoryByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        List<LotteryDrawDTO> history = lotteryService.getDrawHistoryByTimeRange(
                user.getId(), startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success("获取抽奖历史成功", history));
    }
}
