package com.creditapp.service.impl;

import com.creditapp.entity.*;
import com.creditapp.exception.BusinessException;
import com.creditapp.repository.*;
import com.creditapp.service.RedeemResult;
import com.creditapp.service.RedemptionStrategyManager;
import com.creditapp.service.RewardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 礼物兑换服务集成测试
 * 测试策略模式在 RewardService 中的集成
 */
@ExtendWith(MockitoExtension.class)
class RewardServiceIntegrationTest {
    
    @Mock
    private RewardRepository rewardRepository;
    
    @Mock
    private RewardRedemptionRepository rewardRedemptionRepository;
    
    @Mock
    private ChildRepository childRepository;
    
    @Mock
    private PointHistoryRepository pointHistoryRepository;
    
    @Mock
    private RedemptionStrategyManager strategyManager;
    
    @InjectMocks
    private RewardServiceImpl rewardService;
    
    private Reward couponReward;
    private Child child;
    
    @BeforeEach
    void setUp() {
        // 创建上网券礼物
        couponReward = new Reward();
        couponReward.setId(1L);
        couponReward.setName("上网券");
        couponReward.setDescription("2 小时上网时长");
        couponReward.setPointsRequired(100);
        couponReward.setQuantity(10);
        couponReward.setActive(true);
        
        // 创建小孩（有足够积分）
        child = new Child();
        child.setId(1L);
        child.setUsername("test_child");
        child.setPoints(500);
    }
    
    @Test
    void testRedeemReward_Coupon_Success() {
        // 准备数据
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(couponReward));
        when(childRepository.findById(1L)).thenReturn(Optional.of(child));
        
        // 模拟策略管理器返回券码信息
        RedeemResult redeemResult = RedeemResult.success("券码：COUPON_123\n时长：2 小时");
        when(strategyManager.redeem(couponReward, child)).thenReturn(redeemResult);
        
        when(rewardRepository.save(any(Reward.class))).thenReturn(couponReward);
        when(childRepository.save(any(Child.class))).thenReturn(child);
        when(rewardRedemptionRepository.save(any(RewardRedemption.class))).thenAnswer(invocation -> {
            RewardRedemption redemption = invocation.getArgument(0);
            redemption.setId(1L);
            return redemption;
        });
        
        // 执行兑换
        var redemptionDTO = rewardService.redeemReward(1L, 1L);
        
        // 验证结果
        assertNotNull(redemptionDTO);
        assertEquals("上网券", redemptionDTO.getRewardName());
        assertEquals("券码：COUPON_123\n时长：2 小时", redemptionDTO.getNote());
        
        // 验证积分扣除
        assertEquals(400, child.getPoints());
        
        // 验证库存减少
        assertEquals(9, couponReward.getQuantity());
        
        // 验证策略管理器被调用
        verify(strategyManager).redeem(couponReward, child);
        
        // 验证积分历史被记录
        verify(pointHistoryRepository).save(any(PointHistory.class));
    }
    
    @Test
    void testRedeemReward_StrategyFailure() {
        // 准备数据
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(couponReward));
        when(childRepository.findById(1L)).thenReturn(Optional.of(child));
        
        // 模拟策略失败（无可用券码）
        RedeemResult failedResult = RedeemResult.failure("暂无可用上网券");
        when(strategyManager.redeem(couponReward, child)).thenReturn(failedResult);
        
        // 执行兑换，应该抛出异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> rewardService.redeemReward(1L, 1L)
        );
        
        assertEquals("REDEEM_FAILED", exception.getErrorCode());
        assertEquals("暂无可用上网券", exception.getMessage());
        
        // 验证积分未扣除
        assertEquals(500, child.getPoints());
        
        // 验证库存未减少
        assertEquals(10, couponReward.getQuantity());
        
        verify(rewardRedemptionRepository, never()).save(any());
    }
    
    @Test
    void testRedeemReward_InsufficientPoints() {
        // 小孩积分不足
        child.setPoints(50);
        
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(couponReward));
        when(childRepository.findById(1L)).thenReturn(Optional.of(child));
        
        // 执行兑换，应该抛出异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> rewardService.redeemReward(1L, 1L)
        );
        
        assertEquals("INSUFFICIENT_POINTS", exception.getErrorCode());
        
        // 验证策略管理器未被调用
        verify(strategyManager, never()).redeem(any(), any());
    }
    
    @Test
    void testRedeemReward_InactiveReward() {
        // 礼物未激活
        couponReward.setActive(false);
        
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(couponReward));
        when(childRepository.findById(1L)).thenReturn(Optional.of(child));
        
        // 执行兑换，应该抛出异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> rewardService.redeemReward(1L, 1L)
        );
        
        assertEquals("REWARD_INACTIVE", exception.getErrorCode());
        
        verify(strategyManager, never()).redeem(any(), any());
    }
    
    @Test
    void testRedeemReward_OutOfStock() {
        // 库存为 0
        couponReward.setQuantity(0);
        
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(couponReward));
        when(childRepository.findById(1L)).thenReturn(Optional.of(child));
        
        // 执行兑换，应该抛出异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> rewardService.redeemReward(1L, 1L)
        );
        
        assertEquals("INSUFFICIENT_QUANTITY", exception.getErrorCode());
        
        verify(strategyManager, never()).redeem(any(), any());
    }
    
    @Test
    void testRedeemReward_NormalGift_NoNote() {
        // 创建普通礼物
        Reward normalReward = new Reward();
        normalReward.setId(2L);
        normalReward.setName("游戏时间");
        normalReward.setPointsRequired(50);
        normalReward.setQuantity(999);
        normalReward.setActive(true);
        
        when(rewardRepository.findById(2L)).thenReturn(Optional.of(normalReward));
        when(childRepository.findById(1L)).thenReturn(Optional.of(child));
        
        // 模拟普通礼物无特殊逻辑
        RedeemResult redeemResult = RedeemResult.success(null);
        when(strategyManager.redeem(normalReward, child)).thenReturn(redeemResult);
        
        when(rewardRedemptionRepository.save(any(RewardRedemption.class))).thenAnswer(invocation -> {
            RewardRedemption redemption = invocation.getArgument(0);
            redemption.setId(2L);
            return redemption;
        });
        
        // 执行兑换
        var redemptionDTO = rewardService.redeemReward(2L, 1L);
        
        // 验证 note 为空
        assertNotNull(redemptionDTO);
        assertNull(redemptionDTO.getNote());
        
        verify(strategyManager).redeem(normalReward, child);
    }
}
