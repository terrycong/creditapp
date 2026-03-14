package com.creditapp.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 抽奖记录 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotteryDrawDTO {
    
    private Long id;
    
    /**
     * 抽奖主题 ID
     */
    private Long themeId;
    
    /**
     * 抽奖主题名称
     */
    private String themeName;
    
    /**
     * 小孩 ID
     */
    private Long childId;
    
    /**
     * 小孩名称
     */
    private String childName;
    
    /**
     * 消耗的积分
     */
    private Integer pointsCost;
    
    /**
     * 抽奖结果状态
     */
    private String resultStatus;
    
    /**
     * 中奖的奖品列表
     */
    private List<LotteryDrawResultDTO> drawResults;
    
    /**
     * 抽奖时间
     */
    private LocalDateTime drawAt;
}
