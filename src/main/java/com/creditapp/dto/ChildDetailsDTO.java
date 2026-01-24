package com.creditapp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChildDetailsDTO {
    private Long id;
    private String username;
    private Integer points;
    private String parentName;
    private LocalDateTime createdAt;
    
    // Statistics
    private Integer totalTasksCompleted;
    private Integer totalRewardsRedeemed;
    private Integer totalPointsEarned;
    private Integer totalPointsSpent;
    
    // Recent activity
    private List<TaskCompletionDTO> recentTaskCompletions;
    private List<RewardRedemptionDTO> recentRewardRedemptions;
    
    // Current tasks
    private List<TaskDTO> activeTasks;
    
    // Available rewards
    private List<RewardDTO> availableRewards;
}