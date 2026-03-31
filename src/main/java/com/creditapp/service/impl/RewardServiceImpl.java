package com.creditapp.service.impl;

import com.creditapp.dto.*;
import com.creditapp.entity.Child;
import com.creditapp.entity.PointChangeType;
import com.creditapp.entity.PointHistory;
import com.creditapp.entity.Reward;
import com.creditapp.entity.RewardRedemption;
import com.creditapp.exception.BusinessException;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.repository.ChildRepository;
import com.creditapp.repository.PointHistoryRepository;
import com.creditapp.repository.RewardRedemptionRepository;
import com.creditapp.repository.RewardRepository;
import com.creditapp.service.RedeemResult;
import com.creditapp.service.RewardService;
import com.creditapp.service.RedemptionStrategyManager;
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
    private final PointHistoryRepository pointHistoryRepository;
    private final RedemptionStrategyManager redemptionStrategyManager;

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

        // Execute specific redeem logic via strategy pattern
        RedeemResult redeemResult = redemptionStrategyManager.redeem(reward, child);
        
        if (!redeemResult.isSuccess()) {
            throw new BusinessException("REDEEM_FAILED", redeemResult.getErrorMessage());
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
        redemption.setStatus(com.creditapp.entity.RedemptionStatus.REDEEMED);
        // Set note from redeem result (e.g., coupon code details)
        redemption.setNote(redeemResult.getNote());

        rewardRedemptionRepository.save(redemption);
        childRepository.save(child);
        rewardRepository.save(reward);

        // Record point history for reward redemption (consumption)
        PointHistory history = PointHistory.builder()
                .child(child)
                .originalPoints(child.getPoints() + reward.getPointsRequired())
                .changePoints(-reward.getPointsRequired())
                .afterPoints(child.getPoints())
                .changeType(PointChangeType.REWARD_REDEMPTION)
                .description("兑换礼物: " + reward.getName())
                .referenceId(redemption.getId())
                .referenceType("REWARD_REDEMPTION")
                .changedById(childId)
                .createdAt(LocalDateTime.now())
                .build();
        pointHistoryRepository.save(history);
        log.info("Recorded point history for reward redemption: childId={}, rewardId={}, points={}", childId, rewardId, reward.getPointsRequired());

        log.info("Child {} redeemed reward {}, used {} points, note={}", 
                childId, rewardId, reward.getPointsRequired(), redeemResult.getNote());
        return toRedemptionDTO(redemption);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardRedemptionDTO> getRedemptionsByChildId(Long childId) {
        List<RewardRedemption> redemptions = rewardRedemptionRepository.findByChildIdWithReward(childId);
        return redemptions.stream()
                .map(this::toRedemptionDTO)
                .sorted((a, b) -> b.getRedeemedAt().compareTo(a.getRedeemedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RewardRedemptionDTO useReward(Long redemptionId, Long childId) {
        RewardRedemption redemption = rewardRedemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new ResourceNotFoundException("RewardRedemption", redemptionId));

        // Verify the redemption belongs to the child
        if (!redemption.getChild().getId().equals(childId)) {
            throw new BusinessException("UNAUTHORIZED", "无权操作此兑换记录");
        }

        // Check if already used
        if (redemption.getStatus() == com.creditapp.entity.RedemptionStatus.USED) {
            throw new BusinessException("ALREADY_USED", "此礼物已经使用过了");
        }

        // Mark as used
        redemption.setStatus(com.creditapp.entity.RedemptionStatus.USED);
        redemption.setUsedAt(LocalDateTime.now());

        rewardRedemptionRepository.save(redemption);
        log.info("Child {} marked reward redemption {} as used", childId, redemptionId);

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
                .status(redemption.getStatus() != null ? redemption.getStatus().name() : "REDEEMED")
                .usedAt(redemption.getUsedAt())
                .build();
    }
}
