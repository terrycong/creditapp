package com.creditapp.dto;

import com.creditapp.entity.DrawResultStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 抽奖主题 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotteryThemeDTO {
    
    private Long id;
    
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
     * 抽奖类型
     */
    private String type;
    
    /**
     * 是否启用
     */
    private boolean active;
    
    /**
     * 创建者 ID
     */
    private Long createdById;
    
    /**
     * 创建者名称
     */
    private String createdByName;
    
    /**
     * 奖品列表
     */
    private List<LotteryPrizeDTO> prizes;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
