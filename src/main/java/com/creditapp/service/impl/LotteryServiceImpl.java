package com.creditapp.service.impl;

import com.creditapp.dto.*;
import com.creditapp.entity.*;
import com.creditapp.exception.BusinessException;
import com.creditapp.repository.*;
import com.creditapp.service.LotteryService;
import com.creditapp.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LotteryServiceImpl implements LotteryService {

    private static final Logger log = LoggerFactory.getLogger(LotteryServiceImpl.class);

    private final LotteryThemeRepository lotteryThemeRepository;
    private final LotteryPrizeRepository lotteryPrizeRepository;
    private final LotteryDrawRepository lotteryDrawRepository;
    private final LotteryDrawResultRepository lotteryDrawResultRepository;
    private final ChildRepository childRepository;
    private final PointHistoryService pointHistoryService;
    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository rewardRedemptionRepository;
    private final UserRepository userRepository;
    
    private static final Random RANDOM = new Random();

    // ========== 抽奖主题管理 ==========
    
    @Override
    public LotteryThemeDTO createTheme(CreateLotteryThemeRequest request, Long createdById) {
        log.info("Creating lottery theme: name={}, pointsPerDraw={}, type={}", 
                request.getName(), request.getPointsPerDraw(), request.getType());
        
        User creator = userRepository.findById(createdById)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "创建者不存在"));
        
        LotteryTheme theme = LotteryTheme.builder()
                .name(request.getName())
                .description(request.getDescription())
                .pointsPerDraw(request.getPointsPerDraw())
                .type(LotteryTypeEnum.valueOf(request.getType().toUpperCase()))
                .active(true)
                .createdBy(creator)
                .build();
        
        LotteryTheme saved = lotteryThemeRepository.save(theme);
        log.info("Lottery theme created: id={}", saved.getId());
        
        return toDTO(saved);
    }
    
    @Override
    public LotteryThemeDTO updateTheme(Long themeId, CreateLotteryThemeRequest request) {
        log.info("Updating lottery theme: id={}", themeId);
        
        LotteryTheme theme = lotteryThemeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException("THEME_NOT_FOUND", "抽奖主题不存在"));
        
        theme.setName(request.getName());
        theme.setDescription(request.getDescription());
        theme.setPointsPerDraw(request.getPointsPerDraw());
        if (request.getType() != null) {
            theme.setType(LotteryTypeEnum.valueOf(request.getType().toUpperCase()));
        }
        
        LotteryTheme updated = lotteryThemeRepository.save(theme);
        log.info("Lottery theme updated: id={}", updated.getId());
        
        return toDTO(updated);
    }
    
    @Override
    public void deleteTheme(Long themeId) {
        log.info("Deleting lottery theme: id={}", themeId);
        lotteryThemeRepository.deleteById(themeId);
    }
    
    @Override
    public LotteryThemeDTO toggleThemeActive(Long themeId) {
        log.info("Toggling lottery theme active status: id={}", themeId);
        
        LotteryTheme theme = lotteryThemeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException("THEME_NOT_FOUND", "抽奖主题不存在"));
        
        theme.setActive(!theme.isActive());
        LotteryTheme updated = lotteryThemeRepository.save(theme);
        
        log.info("Lottery theme active status toggled: id={}, active={}", updated.getId(), updated.isActive());
        return toDTO(updated);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LotteryThemeDTO> getAllActiveThemes() {
        log.info("Getting all active lottery themes");
        List<LotteryTheme> themes = lotteryThemeRepository.findAllActiveThemes();
        return themes.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LotteryThemeDTO> getAllActiveThemesWithPrizes() {
        log.info("Getting all active lottery themes with prizes");
        List<LotteryTheme> themes = lotteryThemeRepository.findAllActiveThemesWithPrizes();
        return themes.stream().map(this::toDTOWithPrizes).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LotteryThemeDTO> getThemesByParent(Long parentId) {
        log.info("Getting lottery themes for parent: id={}", parentId);
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "家长不存在"));
        List<LotteryTheme> themes = lotteryThemeRepository.findByCreatedBy(parent);
        return themes.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public LotteryThemeDTO getThemeById(Long themeId) {
        log.info("Getting lottery theme by id: id={}", themeId);
        LotteryTheme theme = lotteryThemeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException("THEME_NOT_FOUND", "抽奖主题不存在"));
        return toDTOWithPrizes(theme);
    }
    
    // ========== 奖品管理 ==========
    
    @Override
    public LotteryPrizeDTO createPrize(CreateLotteryPrizeRequest request) {
        log.info("Creating lottery prize: name={}, themeId={}", request.getName(), request.getThemeId());
        
        LotteryTheme theme = lotteryThemeRepository.findById(request.getThemeId())
                .orElseThrow(() -> new BusinessException("THEME_NOT_FOUND", "抽奖主题不存在"));
        
        LotteryPrize prize = LotteryPrize.builder()
                .lotteryTheme(theme)
                .name(request.getName())
                .description(request.getDescription())
                .weight(request.getWeight())
                .probability(request.getProbability())
                .quantity(request.getQuantity())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();
        
        LotteryPrize saved = lotteryPrizeRepository.save(prize);
        log.info("Lottery prize created: id={}", saved.getId());
        
        return toDTO(saved);
    }
    
    @Override
    public LotteryPrizeDTO updatePrize(Long prizeId, CreateLotteryPrizeRequest request) {
        log.info("Updating lottery prize: id={}", prizeId);
        
        LotteryPrize prize = lotteryPrizeRepository.findById(prizeId)
                .orElseThrow(() -> new BusinessException("PRIZE_NOT_FOUND", "奖品不存在"));
        
        if (request.getName() != null) {
            prize.setName(request.getName());
        }
        if (request.getDescription() != null) {
            prize.setDescription(request.getDescription());
        }
        if (request.getWeight() != null) {
            prize.setWeight(request.getWeight());
        }
        if (request.getProbability() != null) {
            prize.setProbability(request.getProbability());
        }
        if (request.getQuantity() != null) {
            prize.setQuantity(request.getQuantity());
        }
        if (request.getImageUrl() != null) {
            prize.setImageUrl(request.getImageUrl());
        }
        
        LotteryPrize updated = lotteryPrizeRepository.save(prize);
        log.info("Lottery prize updated: id={}", updated.getId());
        
        return toDTO(updated);
    }
    
    @Override
    public void deletePrize(Long prizeId) {
        log.info("Deleting lottery prize: id={}", prizeId);
        lotteryPrizeRepository.deleteById(prizeId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LotteryPrizeDTO> getPrizesByTheme(Long themeId) {
        log.info("Getting prizes for theme: id={}", themeId);
        List<LotteryPrize> prizes = lotteryPrizeRepository.findByLotteryThemeAndActiveTrue(
                lotteryThemeRepository.findById(themeId)
                        .orElseThrow(() -> new BusinessException("THEME_NOT_FOUND", "抽奖主题不存在")));
        return prizes.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    // ========== 抽奖功能 ==========
    
    @Override
    public LotteryDrawDTO draw(Long themeId, Long childId) {
        log.info("Performing lottery draw: themeId={}, childId={}", themeId, childId);
        
        // 获取抽奖主题
        LotteryTheme theme = lotteryThemeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException("THEME_NOT_FOUND", "抽奖主题不存在"));
        
        if (!theme.isActive()) {
            throw new BusinessException("THEME_NOT_ACTIVE", "抽奖主题已停用");
        }
        
        // 获取小孩
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new BusinessException("CHILD_NOT_FOUND", "小孩不存在"));
        
        // Use legacy points field for consistency
        int availablePoints = child.getPoints() != null ? child.getPoints() : 0;
        if (availablePoints < theme.getPointsPerDraw()) {
            throw new BusinessException("INSUFFICIENT_POINTS", 
                    "积分不足，需要 " + theme.getPointsPerDraw() + " 积分，当前只有 " + availablePoints + " 积分");
        }
        
        // 获取所有可用奖品
        List<LotteryPrize> prizes = lotteryPrizeRepository.findByLotteryThemeAndActiveTrue(theme);
        if (prizes.isEmpty()) {
            throw new BusinessException("NO_PRIZES_AVAILABLE", "抽奖池中没有可用奖品");
        }
        
        // 执行抽奖算法（权重随机）
        List<LotteryPrize> wonPrizes = performWeightedDraw(prizes);
        
        // Deduct points from legacy points field
        int pointsToSpend = theme.getPointsPerDraw();
        child.setPoints(child.getPoints() - pointsToSpend);
        childRepository.save(child);
        
        // Record point change history
        pointHistoryService.recordPointChange(
                childId,
                -pointsToSpend,
                PointChangeType.LOTTERY_DRAW,
                "抽奖消耗 - " + theme.getName(),
                theme.getId(),
                "LOTTERY_THEME",
                null
        );
        
        // 创建抽奖记录
        LotteryDraw draw = LotteryDraw.builder()
                .lotteryTheme(theme)
                .child(child)
                .pointsCost(theme.getPointsPerDraw())
                .resultStatus(wonPrizes.isEmpty() ? DrawResultStatus.NO_WIN : DrawResultStatus.WON)
                .build();
        draw = lotteryDrawRepository.save(draw);
        
        // 创建中奖结果
        List<LotteryDrawResultDTO> drawResults = new ArrayList<>();
        if (!wonPrizes.isEmpty()) {
            for (LotteryPrize prize : wonPrizes) {
                // 增加奖品兑换计数
                if (prize.getQuantity() != -1) {
                    prize.incrementRedeemed();
                    lotteryPrizeRepository.save(prize);
                }
                
                // 创建抽奖结果记录
                LotteryDrawResult result = LotteryDrawResult.builder()
                        .lotteryDraw(draw)
                        .prize(prize)
                        .prizeName(prize.getName())
                        .rewardId(prize.getRewardId())
                        .build();
                lotteryDrawResultRepository.save(result);
                
                drawResults.add(LotteryDrawResultDTO.builder()
                        .id(result.getId())
                        .prizeId(prize.getId())
                        .prizeName(prize.getName())
                        .rewardId(prize.getRewardId())
                        .build());
                
                // 如果奖品关联了实物礼物，创建兑换记录
                if (prize.getRewardId() != null) {
                    Reward reward = rewardRepository.findById(prize.getRewardId())
                            .orElseThrow(() -> new BusinessException("REWARD_NOT_FOUND", 
                                    "礼物不存在：" + prize.getRewardId()));
                    
                    // 减少礼物库存
                    if (reward.getQuantity() > 0) {
                        reward.setQuantity(reward.getQuantity() - 1);
                        rewardRepository.save(reward);
                    }
                    
                    // 创建礼物兑换记录
                    RewardRedemption redemption = new RewardRedemption();
                    redemption.setReward(reward);
                    redemption.setChild(child);
                    redemption.setRedeemedAt(LocalDateTime.now());
                    redemption.setStatus(RedemptionStatus.REDEEMED);
                    redemption.setNote("抽奖中奖 - " + reward.getName());
                    rewardRedemptionRepository.save(redemption);
                    
                    log.info("Lottery prize redeemed as reward: childId={}, rewardId={}, rewardName={}", 
                            childId, reward.getId(), reward.getName());
                }
            }
        }
        
        log.info("Lottery draw completed: themeId={}, childId={}, won {} prizes", 
                themeId, childId, wonPrizes.size());
        
        return toDTO(draw, drawResults);
    }
    
    /**
     * 执行权重随机抽奖
     */
    private List<LotteryPrize> performWeightedDraw(List<LotteryPrize> prizes) {
        List<LotteryPrize> wonPrizes = new ArrayList<>();
        
        // 计算总权重
        int totalWeight = prizes.stream().mapToInt(LotteryPrize::getWeight).sum();
        if (totalWeight == 0) {
            return wonPrizes;
        }
        
        // 每次抽奖只抽取 1 个奖品
        int numWins = 1; // 固定 1 个奖品
        
        for (int i = 0; i < numWins; i++) {
            int random = RANDOM.nextInt(totalWeight);
            int cumulative = 0;
            
            for (LotteryPrize prize : prizes) {
                cumulative += prize.getWeight();
                if (random < cumulative && prize.hasQuantity()) {
                    wonPrizes.add(prize);
                    break;
                }
            }
        }
        
        return wonPrizes;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LotteryDrawDTO> getDrawHistory(Long childId) {
        log.info("Getting draw history for child: id={}", childId);
        List<LotteryDraw> draws = lotteryDrawRepository.findByChildIdOrderByDrawAtDesc(childId);
        return draws.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LotteryDrawDTO> getDrawHistoryByTimeRange(Long childId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Getting draw history for child in time range: id={}, start={}, end={}", 
                childId, startTime, endTime);
        List<LotteryDraw> draws = lotteryDrawRepository.findByChildIdAndTimeRange(childId, startTime, endTime);
        return draws.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    // ========== 辅助方法 ==========
    
    private LotteryThemeDTO toDTO(LotteryTheme theme) {
        return LotteryThemeDTO.builder()
                .id(theme.getId())
                .name(theme.getName())
                .description(theme.getDescription())
                .pointsPerDraw(theme.getPointsPerDraw())
                .type(theme.getType().name())
                .active(theme.isActive())
                .createdById(theme.getCreatedBy().getId())
                .createdByName(theme.getCreatedBy().getUsername())
                .createdAt(theme.getCreatedAt())
                .updatedAt(theme.getUpdatedAt())
                .build();
    }
    
    private LotteryThemeDTO toDTOWithPrizes(LotteryTheme theme) {
        LotteryThemeDTO dto = toDTO(theme);
        List<LotteryPrizeDTO> prizeDTOs = theme.getPrizes().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        dto.setPrizes(prizeDTOs);
        return dto;
    }
    
    private LotteryPrizeDTO toDTO(LotteryPrize prize) {
        return LotteryPrizeDTO.builder()
                .id(prize.getId())
                .name(prize.getName())
                .description(prize.getDescription())
                .weight(prize.getWeight())
                .probability(prize.getProbability())
                .quantity(prize.getQuantity())
                .redeemedCount(prize.getRedeemedCount())
                .active(prize.isActive())
                .imageUrl(prize.getImageUrl())
                .build();
    }
    
    private LotteryDrawDTO toDTO(LotteryDraw draw) {
        List<LotteryDrawResultDTO> resultDTOs = draw.getDrawResults().stream()
                .map(result -> LotteryDrawResultDTO.builder()
                        .id(result.getId())
                        .prizeId(result.getPrize().getId())
                        .prizeName(result.getPrizeName())
                        .rewardId(result.getRewardId())
                        .build())
                .collect(Collectors.toList());
        
        return toDTO(draw, resultDTOs);
    }
    
    private LotteryDrawDTO toDTO(LotteryDraw draw, List<LotteryDrawResultDTO> resultDTOs) {
        return LotteryDrawDTO.builder()
                .id(draw.getId())
                .themeId(draw.getLotteryTheme().getId())
                .themeName(draw.getLotteryTheme().getName())
                .childId(draw.getChild().getId())
                .childName(draw.getChild().getUsername())
                .pointsCost(draw.getPointsCost())
                .resultStatus(draw.getResultStatus().name())
                .drawResults(resultDTOs)
                .drawAt(draw.getDrawAt())
                .build();
    }
}
