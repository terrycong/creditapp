package com.creditapp.service;

import com.creditapp.dto.CreateTaskRequest;
import com.creditapp.dto.RewardDTO;
import com.creditapp.dto.RewardRedemptionDTO;
import com.creditapp.entity.Child;

import java.util.List;

public interface RewardService {
    RewardDTO createReward(CreateTaskRequest request);
    RewardDTO updateReward(Long rewardId, CreateTaskRequest request);
    void deleteReward(Long rewardId);
    RewardDTO getRewardById(Long id);
    List<RewardDTO> getAllRewards();
    RewardRedemptionDTO redeemReward(Long rewardId, Long childId);
}
