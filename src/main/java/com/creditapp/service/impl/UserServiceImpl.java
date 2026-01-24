package com.creditapp.service.impl;

import com.creditapp.dto.*;
import com.creditapp.entity.*;
import com.creditapp.exception.BusinessException;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.repository.*;
import com.creditapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ChildRepository childRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskCompletionRepository taskCompletionRepository;
    private final RewardRedemptionRepository rewardRedemptionRepository;

    @Override
    @Transactional
    public Child createChild(Long parentId, String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("USERNAME_EXISTS", "用户名已存在");
        }

        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", parentId));

        if (parent.getRole() != UserRole.PARENT) {
            throw new BusinessException("INVALID_ROLE", "只有家长可以创建小孩账号");
        }

        Child child = new Child();
        child.setUsername(username);
        child.setPassword(passwordEncoder.encode(password));
        child.setRole(UserRole.CHILD);
        child.setParent(parent);
        child.setPoints(0);

        return childRepository.save(child);
    }

    @Override
    @Transactional
    public void adjustChildPoints(Long childId, Integer points) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", childId));

        int newPoints = child.getPoints() + points;
        if (newPoints < 0) {
            throw new BusinessException("INSUFFICIENT_POINTS", "积分不能为负数");
        }

        child.setPoints(newPoints);
        log.info("Adjusted points for child {}: {} -> {}", childId, child.getPoints(), newPoints);
    }

    @Override
    public ChildDTO getChildById(Long id) {
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Child", id));

        return ChildDTO.builder()
                .id(child.getId())
                .username(child.getUsername())
                .points(child.getPoints())
                .parentId(child.getParent() != null ? child.getParent().getId() : null)
                .parentName(child.getParent() != null ? child.getParent().getUsername() : null)
                .build();
    }

    @Override
    public List<ChildDTO> getChildrenByParentId(Long parentId) {
        List<Child> children = childRepository.findByParentId(parentId);
        return children.stream()
                .map(child -> ChildDTO.builder()
                        .id(child.getId())
                        .username(child.getUsername())
                        .points(child.getPoints())
                        .parentId(parentId)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public java.util.Optional<User> findByUsername(String username) {
        log.debug("Searching for user with username: {}", username);
        
        // First, try to find in users table
        java.util.Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            log.debug("Found user in users table: {}", username);
            return user;
        }
        
        // If not found in users table, try to find in children table
        java.util.Optional<Child> child = childRepository.findByUsername(username);
        if (child.isPresent()) {
            log.debug("Found child in children table: {}", username);
            // Convert Child to User for authentication
            User childAsUser = new User();
            childAsUser.setId(child.get().getId());
            childAsUser.setUsername(child.get().getUsername());
            childAsUser.setPassword(child.get().getPassword());
            childAsUser.setRole(child.get().getRole());
            childAsUser.setPoints(child.get().getPoints());
            childAsUser.setParent(child.get().getParent());
            return java.util.Optional.of(childAsUser);
        }
        
        log.debug("User not found in either table: {}", username);
        return java.util.Optional.empty();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    public ChildDTO updateChild(Long childId, UpdateChildRequest request) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", childId));
        
        // Update username if provided
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            // Check if new username is already taken
            if (!child.getUsername().equals(request.getUsername()) && 
                userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException("USERNAME_EXISTS", "用户名已存在");
            }
            child.setUsername(request.getUsername());
        }
        
        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            child.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // Update points if provided
        if (request.getPoints() != null) {
            child.setPoints(request.getPoints());
        }
        
        Child updatedChild = childRepository.save(child);
        
        return ChildDTO.builder()
                .id(updatedChild.getId())
                .username(updatedChild.getUsername())
                .role(updatedChild.getRole())
                .points(updatedChild.getPoints())
                .parentId(updatedChild.getParent() != null ? updatedChild.getParent().getId() : null)
                .parentName(updatedChild.getParent() != null ? updatedChild.getParent().getUsername() : null)
                .build();
    }

    @Override
    public void deleteChild(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", childId));
        
        // Check if child has any task completions or reward redemptions
        // In a real application, you might want to handle cascading deletes or soft deletes
        // For now, we'll just delete the child
        
        childRepository.delete(child);
        log.info("Deleted child with id: {}", childId);
    }

    @Override
    public ChildDetailsDTO getChildDetails(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", childId));
        
        // Get task completions for this child
        List<TaskCompletion> taskCompletions = taskCompletionRepository.findByChildId(childId);
        int totalTasksCompleted = (int) taskCompletions.stream()
                .filter(tc -> tc.getStatus() == CompletionStatus.APPROVED)
                .count();
        
        // Get reward redemptions for this child
        List<RewardRedemption> rewardRedemptions = rewardRedemptionRepository.findByChildId(childId);
        int totalRewardsRedeemed = rewardRedemptions.size();
        
        // Calculate points
        int totalPointsEarned = taskCompletions.stream()
                .filter(tc -> tc.getStatus() == CompletionStatus.APPROVED)
                .mapToInt(tc -> tc.getTask().getPoints())
                .sum();
        
        int totalPointsSpent = rewardRedemptions.stream()
                .mapToInt(rr -> rr.getReward().getPointsRequired())
                .sum();
        
        // Get recent activity (last 5)
        List<TaskCompletionDTO> recentTaskCompletions = taskCompletions.stream()
                .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt()))
                .limit(5)
                .map(this::convertToTaskCompletionDTO)
                .collect(Collectors.toList());
        
        List<RewardRedemptionDTO> recentRewardRedemptions = rewardRedemptions.stream()
                .sorted((a, b) -> b.getRedeemedAt().compareTo(a.getRedeemedAt()))
                .limit(5)
                .map(this::convertToRewardRedemptionDTO)
                .collect(Collectors.toList());
        
        return ChildDetailsDTO.builder()
                .id(child.getId())
                .username(child.getUsername())
                .points(child.getPoints())
                .parentName(child.getParent() != null ? child.getParent().getUsername() : null)
                .totalTasksCompleted(totalTasksCompleted)
                .totalRewardsRedeemed(totalRewardsRedeemed)
                .totalPointsEarned(totalPointsEarned)
                .totalPointsSpent(totalPointsSpent)
                .recentTaskCompletions(recentTaskCompletions)
                .recentRewardRedemptions(recentRewardRedemptions)
                .build();
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
