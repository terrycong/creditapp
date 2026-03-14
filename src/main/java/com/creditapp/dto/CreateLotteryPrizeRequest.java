package com.creditapp.dto;

import lombok.*;

/**
 * 创建/更新奖品请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLotteryPrizeRequest {
    
    /**
     * 主题 ID
     */
    private Long themeId;
    
    /**
     * 奖品名称
     */
    private String name;
    
    /**
     * 奖品描述
     */
    private String description;
    
    /**
     * 奖品价值（积分）
     */
    private Integer value;
    
    /**
     * 概率权重
     */
    private Integer weight;
    
    /**
     * 固定概率（百分比，0-100）
     */
    private Integer probability;
    
    /**
     * 库存数量（-1 表示无限）
     */
    private Integer quantity;
    
    /**
     * 奖品图片 URL
     */
    private String imageUrl;
}
