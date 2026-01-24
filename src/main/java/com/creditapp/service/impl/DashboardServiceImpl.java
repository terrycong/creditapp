package com.creditapp.service.impl;

import com.creditapp.dto.*;
import com.creditapp.entity.*;
import com.creditapp.repository.*;
import com.creditapp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ChildRepository childRepository;
    private final TaskRepository taskRepository;
    private final RewardRepository rewardRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final RewardRedemptionRepository rewardRedemptionRepository;

    @Override
    public DashboardStatsDTO getParentDashboardStats(Long parentId) {
        log.info("Getting dashboard statistics for parent: {}", parentId);
        
        DashboardStatsDTO.DashboardStatsDTOBuilder builder = DashboardStatsDTO.builder();
        
        // Get basic statistics
        List<Child> children = childRepository.findByParentId(parentId);
        List<Task> tasks = taskRepository.findByCreatedBy_Id(parentId);
        List<Reward> rewards = rewardRepository.findAll();
        
        builder.totalChildren(children.size())
               .totalTasksCreated(tasks.size())
               .totalRewardsAvailable(rewards.size());
        
        // Get recent task completions (last 10)
        Pageable recentLimit = PageRequest.of(0, 10);
        List<TaskCompletion> recentCompletions = taskCompletionRepository
                .findRecentCompletionsByParentIdWithLimit(parentId, recentLimit);
        
        List<TaskCompletionDTO> taskCompletionDTOs = recentCompletions.stream()
                .map(this::convertToTaskCompletionDTO)
                .collect(Collectors.toList());
        builder.recentTaskCompletions(taskCompletionDTOs);
        
        // Get recent reward redemptions (last 10)
        List<RewardRedemption> recentRedemptions = rewardRedemptionRepository
                .findRecentRedemptionsByParentIdWithLimit(parentId, recentLimit);
        
        List<RewardRedemptionDTO> rewardRedemptionDTOs = recentRedemptions.stream()
                .map(this::convertToRewardRedemptionDTO)
                .collect(Collectors.toList());
        builder.recentRewardRedemptions(rewardRedemptionDTOs);
        
        // Get child activity statistics
        List<ChildActivityDTO> childActivities = getChildActivityStats(parentId, children);
        builder.childActivities(childActivities);
        
        // Get popular items
        Map<String, Integer> taskPopularity = getTaskPopularity(parentId);
        Map<String, Integer> rewardPopularity = getRewardPopularity(parentId);
        
        if (!taskPopularity.isEmpty()) {
            Map.Entry<String, Integer> mostPopularTask = taskPopularity.entrySet().iterator().next();
            builder.mostPopularTask(mostPopularTask.getKey())
                   .mostPopularTaskCount(mostPopularTask.getValue());
        }
        
        if (!rewardPopularity.isEmpty()) {
            Map.Entry<String, Integer> mostPopularReward = rewardPopularity.entrySet().iterator().next();
            builder.mostPopularReward(mostPopularReward.getKey())
                   .mostPopularRewardCount(mostPopularReward.getValue());
        }
        
        // Get today's statistics
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        int tasksCompletedToday = (int) recentCompletions.stream()
                .filter(tc -> tc.getCompletedAt() != null && 
                             tc.getCompletedAt().isAfter(startOfDay) && 
                             tc.getCompletedAt().isBefore(endOfDay))
                .count();
        
        int rewardsRedeemedToday = (int) recentRedemptions.stream()
                .filter(rr -> rr.getRedeemedAt() != null && 
                             rr.getRedeemedAt().isAfter(startOfDay) && 
                             rr.getRedeemedAt().isBefore(endOfDay))
                .count();
        
        int pointsEarnedToday = recentCompletions.stream()
                .filter(tc -> tc.getCompletedAt() != null && 
                             tc.getCompletedAt().isAfter(startOfDay) && 
                             tc.getCompletedAt().isBefore(endOfDay))
                .mapToInt(tc -> tc.getTask().getPoints())
                .sum();
        
        int pointsSpentToday = recentRedemptions.stream()
                .filter(rr -> rr.getRedeemedAt() != null && 
                             rr.getRedeemedAt().isAfter(startOfDay) && 
                             rr.getRedeemedAt().isBefore(endOfDay))
                .mapToInt(rr -> rr.getReward().getPointsRequired())
                .sum();
        
        builder.tasksCompletedToday(tasksCompletedToday)
               .rewardsRedeemedToday(rewardsRedeemedToday)
               .pointsEarnedToday(pointsEarnedToday)
               .pointsSpentToday(pointsSpentToday);
        
        // Calculate total points distributed and redeemed
        int totalPointsDistributed = recentCompletions.stream()
                .mapToInt(tc -> tc.getTask().getPoints())
                .sum();
        
        int totalPointsRedeemed = recentRedemptions.stream()
                .mapToInt(rr -> rr.getReward().getPointsRequired())
                .sum();
        
        builder.totalPointsDistributed(totalPointsDistributed)
               .totalPointsRedeemed(totalPointsRedeemed);
        
        return builder.build();
    }

    @Override
    public DashboardStatsDTO getChildDashboardStats(Long childId) {
        log.info("Getting dashboard statistics for child: {}", childId);
        
        DashboardStatsDTO.DashboardStatsDTOBuilder builder = DashboardStatsDTO.builder();
        
        // Get child's recent task completions
        List<TaskCompletion> childCompletions = taskCompletionRepository.findByChildId(childId);
        List<TaskCompletionDTO> taskCompletionDTOs = childCompletions.stream()
                .map(this::convertToTaskCompletionDTO)
                .collect(Collectors.toList());
        builder.recentTaskCompletions(taskCompletionDTOs);
        
        // Get child's recent reward redemptions
        List<RewardRedemption> childRedemptions = rewardRedemptionRepository.findByChildIdWithReward(childId);
        List<RewardRedemptionDTO> rewardRedemptionDTOs = childRedemptions.stream()
                .map(this::convertToRewardRedemptionDTO)
                .collect(Collectors.toList());
        builder.recentRewardRedemptions(rewardRedemptionDTOs);
        
        // Get child's current points
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found: " + childId));
        
        // Create a simple child activity DTO
        ChildActivityDTO childActivity = ChildActivityDTO.builder()
                .childId(childId)
                .childName(child.getUsername())
                .taskCompletionCount(childCompletions.size())
                .rewardRedemptionCount(childRedemptions.size())
                .currentPoints(child.getPoints())
                .build();
        
        builder.childActivities(List.of(childActivity));
        
        return builder.build();
    }

    private List<ChildActivityDTO> getChildActivityStats(Long parentId, List<Child> children) {
        List<ChildActivityDTO> childActivities = new ArrayList<>();
        
        // Get task completion counts by child
        List<Object[]> completionCounts = taskCompletionRepository.countCompletionsByChildForParent(parentId);
        Map<Long, Integer> completionCountMap = new HashMap<>();
        for (Object[] result : completionCounts) {
            Long childId = (Long) result[0];
            Long count = (Long) result[2];
            completionCountMap.put(childId, count.intValue());
        }
        
        // Get reward redemption counts and points spent by child
        List<Object[]> redemptionCounts = rewardRedemptionRepository.countRedemptionsByChildForParent(parentId);
        Map<Long, Integer> redemptionCountMap = new HashMap<>();
        Map<Long, Integer> pointsSpentMap = new HashMap<>();
        for (Object[] result : redemptionCounts) {
            Long childId = (Long) result[0];
            Long count = (Long) result[2];
            Long pointsSpent = (Long) result[3];
            redemptionCountMap.put(childId, count.intValue());
            pointsSpentMap.put(childId, pointsSpent.intValue());
        }
        
        // Calculate total points earned by child
        List<TaskCompletion> allCompletions = taskCompletionRepository.findRecentCompletionsByParentId(parentId);
        Map<Long, Integer> pointsEarnedMap = new HashMap<>();
        for (TaskCompletion tc : allCompletions) {
            Long childId = tc.getChild().getId();
            int points = tc.getTask().getPoints();
            pointsEarnedMap.put(childId, pointsEarnedMap.getOrDefault(childId, 0) + points);
        }
        
        // Build ChildActivityDTO for each child
        for (Child child : children) {
            Long childId = child.getId();
            
            ChildActivityDTO.ChildActivityDTOBuilder childBuilder = ChildActivityDTO.builder()
                    .childId(childId)
                    .childName(child.getUsername())
                    .taskCompletionCount(completionCountMap.getOrDefault(childId, 0))
                    .rewardRedemptionCount(redemptionCountMap.getOrDefault(childId, 0))
                    .totalPointsEarned(pointsEarnedMap.getOrDefault(childId, 0))
                    .totalPointsSpent(pointsSpentMap.getOrDefault(childId, 0))
                    .currentPoints(child.getPoints());
            
            // Find most completed task for this child
            Map<String, Integer> childTaskCounts = new HashMap<>();
            for (TaskCompletion tc : allCompletions) {
                if (tc.getChild().getId().equals(childId)) {
                    String taskTitle = tc.getTask().getTitle();
                    childTaskCounts.put(taskTitle, childTaskCounts.getOrDefault(taskTitle, 0) + 1);
                }
            }
            
            if (!childTaskCounts.isEmpty()) {
                Map.Entry<String, Integer> mostCompleted = childTaskCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .orElse(null);
                if (mostCompleted != null) {
                    childBuilder.mostCompletedTask(mostCompleted.getKey())
                               .mostCompletedTaskCount(mostCompleted.getValue());
                }
            }
            
            // Find most redeemed reward for this child
            List<RewardRedemption> childRedemptions = rewardRedemptionRepository.findByChildId(childId);
            Map<String, Integer> childRewardCounts = new HashMap<>();
            for (RewardRedemption rr : childRedemptions) {
                String rewardName = rr.getReward().getName();
                childRewardCounts.put(rewardName, childRewardCounts.getOrDefault(rewardName, 0) + 1);
            }
            
            if (!childRewardCounts.isEmpty()) {
                Map.Entry<String, Integer> mostRedeemed = childRewardCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .orElse(null);
                if (mostRedeemed != null) {
                    childBuilder.mostRedeemedReward(mostRedeemed.getKey())
                               .mostRedeemedRewardCount(mostRedeemed.getValue());
                }
            }
            
            // Find last activity timestamps
            Optional<TaskCompletion> lastTaskCompletion = allCompletions.stream()
                    .filter(tc -> tc.getChild().getId().equals(childId))
                    .max(Comparator.comparing(TaskCompletion::getCompletedAt));
            
            Optional<RewardRedemption> lastRewardRedemption = childRedemptions.stream()
                    .max(Comparator.comparing(RewardRedemption::getRedeemedAt));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            lastTaskCompletion.ifPresent(tc -> 
                childBuilder.lastTaskCompletedAt(tc.getCompletedAt().format(formatter)));
            lastRewardRedemption.ifPresent(rr -> 
                childBuilder.lastRewardRedeemedAt(rr.getRedeemedAt().format(formatter)));
            
            childActivities.add(childBuilder.build());
        }
        
        return childActivities;
    }

    private Map<String, Integer> getTaskPopularity(Long parentId) {
        List<TaskCompletion> completions = taskCompletionRepository.findRecentCompletionsByParentId(parentId);
        Map<String, Integer> taskCounts = new HashMap<>();
        
        for (TaskCompletion tc : completions) {
            String taskTitle = tc.getTask().getTitle();
            taskCounts.put(taskTitle, taskCounts.getOrDefault(taskTitle, 0) + 1);
        }
        
        // Sort by count descending
        return taskCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private Map<String, Integer> getRewardPopularity(Long parentId) {
        List<Object[]> rewardStats = rewardRedemptionRepository.getRewardRedemptionStats(parentId);
        Map<String, Integer> rewardCounts = new LinkedHashMap<>();
        
        for (Object[] result : rewardStats) {
            String rewardName = (String) result[0];
            Long count = (Long) result[1];
            rewardCounts.put(rewardName, count.intValue());
        }
        
        return rewardCounts;
    }

    private TaskCompletionDTO convertToTaskCompletionDTO(TaskCompletion taskCompletion) {
        return TaskCompletionDTO.builder()
                .id(taskCompletion.getId())
                .taskId(taskCompletion.getTask().getId())
                .taskTitle(taskCompletion.getTask().getTitle())
                .taskPoints(taskCompletion.getTask().getPoints())
                .childId(taskCompletion.getChild().getId())
                .childName(taskCompletion.getChild().getUsername())
                .status(taskCompletion.getStatus())
                .proof(taskCompletion.getProof())
                .completedAt(taskCompletion.getCompletedAt())
                .approvedAt(taskCompletion.getApprovedAt())
                .build();
    }

    private RewardRedemptionDTO convertToRewardRedemptionDTO(RewardRedemption rewardRedemption) {
        return RewardRedemptionDTO.builder()
                .id(rewardRedemption.getId())
                .rewardId(rewardRedemption.getReward().getId())
                .rewardName(rewardRedemption.getReward().getName())
                .pointsRequired(rewardRedemption.getReward().getPointsRequired())
                .childId(rewardRedemption.getChild().getId())
                .childName(rewardRedemption.getChild().getUsername())
                .redeemedAt(rewardRedemption.getRedeemedAt())
                .note(rewardRedemption.getNote())
                .build();
    }
}