package com.creditapp.dto;

import lombok.*;

/**
 * 创建抽奖主题请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLotteryThemeRequest {
    
    /**
     * 主题名称
     */
    private String name;
    
    /**
     * 主题描述
     */
    private String description;
    
    /**
     * 每次抽奖消耗的积分
     */
    private Integer pointsPerDraw;
    
    /**
     * 抽奖类型（FIXED_PROBABILITY, WEIGHTED_RANDOM, GUARANTEED）
     */
    private String type;
}
