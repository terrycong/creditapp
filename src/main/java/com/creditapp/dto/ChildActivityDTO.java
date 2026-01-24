package com.creditapp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChildActivityDTO {
    private Long childId;
    private String childName;
    private Integer taskCompletionCount;
    private Integer totalPointsEarned;
    private Integer rewardRedemptionCount;
    private Integer totalPointsSpent;
    private Integer currentPoints;
    
    // For task completion statistics
    private String mostCompletedTask;
    private Integer mostCompletedTaskCount;
    
    // For reward redemption statistics
    private String mostRedeemedReward;
    private Integer mostRedeemedRewardCount;
    
    // Recent activity timestamps
    private String lastTaskCompletedAt;
    private String lastRewardRedeemedAt;
}