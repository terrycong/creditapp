package com.creditapp.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Point Wallet DTO
 * Represents a batch of points with expiration information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointWalletDTO {
    
    private Long id;
    
    /**
     * Child ID
     */
    private Long childId;
    
    /**
     * Original points in this batch
     */
    private Integer originalPoints;
    
    /**
     * Remaining points (after spending)
     */
    private Integer remainingPoints;
    
    /**
     * Date when points were earned
     */
    private LocalDateTime earnedDate;
    
    /**
     * Expiration date (null if no expiration)
     */
    private LocalDate expirationDate;
    
    /**
     * Source type (TASK_COMPLETION, BONUS, etc.)
     */
    private String sourceType;
    
    /**
     * Source entity ID
     */
    private Long sourceId;
    
    /**
     * Whether all points have been spent
     */
    private Boolean fullySpent;
    
    /**
     * Whether points have expired
     */
    private Boolean expired;
    
    /**
     * Date when points expired
     */
    private LocalDateTime expiredDate;
    
    /**
     * Days until expiration (calculated field, for display)
     */
    private Integer daysUntilExpiration;
    
    /**
     * Whether points are expiring soon (within 7 days)
     */
    private Boolean expiringSoon;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;

    /**
     * Get description based on source type
     */
    public String getDescription() {
        if (sourceType == null) {
            return "其他";
        }
        switch (sourceType) {
            case "TASK_COMPLETION":
                return "完成任务";
            case "LOTTERY_WIN":
                return "抽奖中奖";
            case "REWARD_REDEMPTION":
                return "礼物兑换";
            case "INITIAL":
                return "初始积分";
            case "MANUAL_ADJUSTMENT":
                return "手动调整";
            case "BONUS":
                return "奖励";
            default:
                return "其他";
        }
    }
}
