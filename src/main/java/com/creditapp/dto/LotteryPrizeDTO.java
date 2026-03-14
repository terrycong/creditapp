package com.creditapp.dto;

import lombok.*;

/**
 * 抽奖奖品 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotteryPrizeDTO {
    
    private Long id;
    
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
     * 固定概率（百分比）
     */
    private Integer probability;
    
    /**
     * 库存数量（-1 表示无限）
     */
    private Integer quantity;
    
    /**
     * 已兑换数量
     */
    private Integer redeemedCount;
    
    /**
     * 是否启用
     */
    private boolean active;
    
    /**
     * 奖品图片 URL
     */
    private String imageUrl;
}
