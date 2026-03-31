package com.creditapp.service;

import com.creditapp.entity.Child;
import com.creditapp.entity.Reward;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 兑换策略管理器测试
 */
@ExtendWith(MockitoExtension.class)
class RedemptionStrategyManagerTest {
    
    private RedemptionStrategyManager strategyManager;
    
    private RedeemableService couponService;
    
    private RedeemableService defaultService;
    
    private Reward couponReward;
    private Reward normalReward;
    private Child child;
    
    @BeforeEach
    void setUp() {
        // 创建 mock 服务
        couponService = mock(RedeemableService.class);
        defaultService = mock(RedeemableService.class);
        
        // 创建策略管理器（手动注入列表）
        strategyManager = new RedemptionStrategyManager(java.util.Arrays.asList(couponService, defaultService));
        
        // 创建上网券礼物
        couponReward = new Reward();
        couponReward.setId(1L);
        couponReward.setName("上网券");
        couponReward.setDescription("2 小时上网时长");
        couponReward.setPointsRequired(100);
        
        // 创建普通礼物
        normalReward = new Reward();
        normalReward.setId(2L);
        normalReward.setName("游戏时间");
        normalReward.setDescription("30 分钟游戏时间");
        normalReward.setPointsRequired(50);
        
        // 创建小孩
        child = new Child();
        child.setId(1L);
        child.setUsername("test_child");
    }
    
    @Test
    void testRedeem_CouponService() {
        // 模拟 couponService 支持上网券
        when(couponService.supports(couponReward)).thenReturn(true);
        when(couponService.getOrder()).thenReturn(10);
        when(couponService.redeem(couponReward, child)).thenReturn(RedeemResult.success("券码：TEST123"));
        
        // 执行测试
        RedeemResult result = strategyManager.redeem(couponReward, child);
        
        // 验证
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("券码：TEST123", result.getNote());
        
        // 验证调用了 couponService（优先级高）
        verify(couponService).redeem(couponReward, child);
    }
    
    @Test
    void testRedeem_DefaultService() {
        // 模拟 couponService 不支持普通礼物
        when(couponService.supports(normalReward)).thenReturn(false);
        when(couponService.getOrder()).thenReturn(10);
        
        when(defaultService.supports(normalReward)).thenReturn(true);
        when(defaultService.getOrder()).thenReturn(1000);
        when(defaultService.redeem(normalReward, child)).thenReturn(RedeemResult.success(null));
        
        // 执行测试
        RedeemResult result = strategyManager.redeem(normalReward, child);
        
        // 验证
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNull(result.getNote());
        
        // 验证调用了 defaultService
        verify(defaultService).redeem(normalReward, child);
    }
    
    @Test
    void testRedeem_NoServiceFound() {
        // 模拟所有服务都不支持
        when(couponService.supports(normalReward)).thenReturn(false);
        when(defaultService.supports(normalReward)).thenReturn(false);
        
        // 执行测试
        RedeemResult result = strategyManager.redeem(normalReward, child);
        
        // 验证
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("不支持的礼物类型", result.getErrorMessage());
    }
    
    @Test
    void testServiceOrder() {
        // 创建多个服务，测试优先级
        RedeemableService highPriority = mock(RedeemableService.class);
        RedeemableService lowPriority = mock(RedeemableService.class);
        
        when(highPriority.supports(couponReward)).thenReturn(true);
        when(highPriority.getOrder()).thenReturn(1);
        when(highPriority.redeem(couponReward, child)).thenReturn(RedeemResult.success("high"));
        
        List<RedeemableService> services = Arrays.asList(lowPriority, highPriority);
        RedemptionStrategyManager manager = new RedemptionStrategyManager(services);
        
        manager.redeem(couponReward, child);
        
        // 验证高优先级服务先被调用
        verify(highPriority).redeem(couponReward, child);
    }
}
