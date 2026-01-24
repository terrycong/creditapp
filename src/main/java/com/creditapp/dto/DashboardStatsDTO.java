package com.creditapp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardStatsDTO {
    // Overall statistics
    private Integer totalChildren;
    private Integer totalTasksCreated;
    private Integer totalRewardsAvailable;
    private Integer totalPointsDistributed;
    private Integer totalPointsRedeemed;
    
    // Recent activity
    private List<TaskCompletionDTO> recentTaskCompletions;
    private List<RewardRedemptionDTO> recentRewardRedemptions;
    
    // Child activity statistics
    private List<ChildActivityDTO> childActivities;
    
    // Popular items
    private String mostPopularTask;
    private Integer mostPopularTaskCount;
    private String mostPopularReward;
    private Integer mostPopularRewardCount;
    
    // Time-based statistics
    private Integer tasksCompletedToday;
    private Integer rewardsRedeemedToday;
    private Integer pointsEarnedToday;
    private Integer pointsSpentToday;
    
    // For charts and visualizations
    private List<DailyActivityDTO> weeklyActivity;
    
    @Data
    @Builder
    public static class DailyActivityDTO {
        private String date;
        private Integer tasksCompleted;
        private Integer rewardsRedeemed;
        private Integer pointsEarned;
        private Integer pointsSpent;
    }
}