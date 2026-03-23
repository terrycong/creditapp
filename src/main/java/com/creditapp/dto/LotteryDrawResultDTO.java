package com.creditapp.dto;

import lombok.*;

/**
 * 抽奖结果详情 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotteryDrawResultDTO {
    
    private Long id;
    
    /**
     * 奖品 ID
     */
    private Long prizeId;
    
    /**
     * 奖品名称
     */
    private String prizeName;
    
    /**
     * 关联的礼物 ID（如果奖品是实物礼物）
     */
    private Long rewardId;
}
