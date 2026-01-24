package com.creditapp.service.impl;

import com.creditapp.dto.*;
import com.creditapp.entity.Child;
import com.creditapp.entity.Reward;
import com.creditapp.entity.RewardRedemption;
import com.creditapp.exception.BusinessException;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.repository.ChildRepository;
import com.creditapp.repository.RewardRedemptionRepository;
import com.creditapp.repository.RewardRepository;
import com.creditapp.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private static final Logger log = LoggerFactory.getLogger(RewardServiceImpl.class);

    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository rewardRedemptionRepository;
    private final ChildRepository childRepository;

    @Override
    @Transactional
    public RewardDTO createReward(CreateTaskRequest request) {
        Reward reward = new Reward();
        reward.setName(request.getTitle());
        reward.setDescription(request.getDescription());
        reward.setQuantity(999);
        reward.setPointsRequired(request.getPoints());
        reward.setActive(true);

        Reward saved = rewardRepository.save(reward);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public RewardDTO updateReward(Long rewardId, CreateTaskRequest request) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", rewardId));

        reward.setName(request.getTitle());
        reward.setDescription(request.getDescription());
        reward.setPointsRequired(request.getPoints());

        Reward saved = rewardRepository.save(reward);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteReward(Long rewardId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", rewardId));
        rewardRepository.delete(reward);
    }

    @Override
    public RewardDTO getRewardById(Long id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", id));
        return toDTO(reward);
    }

    @Override
    public List<RewardDTO> getAllRewards() {
        List<Reward> rewards = rewardRepository.findByActiveTrue();
        return rewards.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RewardRedemptionDTO redeemReward(Long rewardId, Long childId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", rewardId));

        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", childId));

        if (!reward.isActive()) {
            throw new BusinessException("REWARD_INACTIVE", "礼物未激活");
        }

        if (reward.getQuantity() <= 0) {
            throw new BusinessException("INSUFFICIENT_QUANTITY", "礼物库存不足");
        }

        if (child.getPoints() < reward.getPointsRequired()) {
            throw new BusinessException("INSUFFICIENT_POINTS", "积分不足");
        }

        // Deduct points
        child.setPoints(child.getPoints() - reward.getPointsRequired());

        // Reduce quantity
        reward.setQuantity(reward.getQuantity() - 1);

        // Create redemption record
        RewardRedemption redemption = new RewardRedemption();
        redemption.setReward(reward);
        redemption.setChild(child);
        redemption.setRedeemedAt(LocalDateTime.now());

        rewardRedemptionRepository.save(redemption);
        childRepository.save(child);
        rewardRepository.save(reward);

        log.info("Child {} redeemed reward {}, used {} points", childId, rewardId, reward.getPointsRequired());
        return toRedemptionDTO(redemption);
    }

    private RewardDTO toDTO(Reward reward) {
        return RewardDTO.builder()
                .id(reward.getId())
                .name(reward.getName())
                .description(reward.getDescription())
                .quantity(reward.getQuantity())
                .pointsRequired(reward.getPointsRequired())
                .imageUrl(reward.getImageUrl())
                .active(reward.isActive())
                .build();
    }

    private RewardRedemptionDTO toRedemptionDTO(RewardRedemption redemption) {
        return RewardRedemptionDTO.builder()
                .id(redemption.getId())
                .rewardId(redemption.getReward().getId())
                .rewardName(redemption.getReward().getName())
                .pointsRequired(redemption.getReward().getPointsRequired())
                .childId(redemption.getChild().getId())
                .childName(redemption.getChild().getUsername())
                .redeemedAt(redemption.getRedeemedAt())
                .note(redemption.getNote())
                .build();
    }
}
