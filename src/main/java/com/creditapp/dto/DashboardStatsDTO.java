package com.creditapp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

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
    
    // Weekly summary statistics
    private Integer weeklyTasksCompleted;
    private Integer weeklyRewardsRedeemed;
    private Integer weeklyPointsEarned;
    private Integer weeklyPointsSpent;
    
    // Task type distribution statistics
    private Map<String, Integer> taskTypeDistribution;
    private Double averagePointsPerTask;
    private String mostRewardingTaskType;
    private Integer mostRewardingTaskTypePoints;
    
    // Point distribution by child
    private Map<String, Integer> pointsDistributionByChild;
    
    // Point expiration statistics
    private Integer totalExpiredPoints;
    private Integer totalExpiringSoonPoints;
    private Map<String, Integer> expiredPointsByChild;
    private Map<String, Integer> expiringSoonPointsByChild;
    
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