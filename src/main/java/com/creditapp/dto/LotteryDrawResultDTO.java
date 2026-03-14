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
     * 奖品价值
     */
    private Integer prizeValue;
}
