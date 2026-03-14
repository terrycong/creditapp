package com.creditapp.service;

import com.creditapp.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface LotteryService {
    
    // ========== 抽奖主题管理 ==========
    
    /**
     * 创建抽奖主题
     */
    LotteryThemeDTO createTheme(CreateLotteryThemeRequest request, Long createdById);
    
    /**
     * 更新抽奖主题
     */
    LotteryThemeDTO updateTheme(Long themeId, CreateLotteryThemeRequest request);
    
    /**
     * 删除抽奖主题
     */
    void deleteTheme(Long themeId);
    
    /**
     * 启用/禁用抽奖主题
     */
    LotteryThemeDTO toggleThemeActive(Long themeId);
    
    /**
     * 获取所有启用的抽奖主题（小孩查看）
     */
    List<LotteryThemeDTO> getAllActiveThemes();
    
    /**
     * 获取所有抽奖主题（包含奖品列表）
     */
    List<LotteryThemeDTO> getAllActiveThemesWithPrizes();
    
    /**
     * 获取家长创建的所有抽奖主题
     */
    List<LotteryThemeDTO> getThemesByParent(Long parentId);
    
    /**
     * 获取抽奖主题详情
     */
    LotteryThemeDTO getThemeById(Long themeId);
    
    // ========== 奖品管理 ==========
    
    /**
     * 创建奖品
     */
    LotteryPrizeDTO createPrize(CreateLotteryPrizeRequest request);
    
    /**
     * 更新奖品
     */
    LotteryPrizeDTO updatePrize(Long prizeId, CreateLotteryPrizeRequest request);
    
    /**
     * 删除奖品
     */
    void deletePrize(Long prizeId);
    
    /**
     * 获取抽奖主题下的所有奖品
     */
    List<LotteryPrizeDTO> getPrizesByTheme(Long themeId);
    
    // ========== 抽奖功能 ==========
    
    /**
     * 执行抽奖
     * @param themeId 抽奖主题 ID
     * @param childId 小孩 ID
     * @return 抽奖结果
     */
    LotteryDrawDTO draw(Long themeId, Long childId);
    
    /**
     * 获取小孩的抽奖历史记录
     */
    List<LotteryDrawDTO> getDrawHistory(Long childId);
    
    /**
     * 获取指定时间范围内的抽奖历史
     */
    List<LotteryDrawDTO> getDrawHistoryByTimeRange(Long childId, LocalDateTime startTime, LocalDateTime endTime);
}
