package com.creditapp.service;

import com.creditapp.entity.Child;
import com.creditapp.entity.Reward;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 默认礼物兑换服务测试
 */
class DefaultRedeemServiceTest {
    
    private DefaultRedeemService defaultService;
    private Reward reward;
    private Child child;
    
    @BeforeEach
    void setUp() {
        defaultService = new DefaultRedeemService();
        
        reward = new Reward();
        reward.setId(1L);
        reward.setName("测试礼物");
        reward.setDescription("测试描述");
        reward.setPointsRequired(100);
        
        child = new Child();
        child.setId(1L);
        child.setUsername("test_child");
    }
    
    @Test
    void testSupports_AlwaysTrue() {
        assertTrue(defaultService.supports(reward));
        
        Reward nullReward = null;
        assertTrue(defaultService.supports(nullReward));
    }
    
    @Test
    void testRedeem_ReturnsSuccessWithNullNote() {
        RedeemResult result = defaultService.redeem(reward, child);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNull(result.getNote());
        assertNull(result.getData());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    void testGetOrder() {
        assertEquals(1000, defaultService.getOrder());
    }
    
    @Test
    void testRedeem_DifferentRewards() {
        Reward reward1 = new Reward();
        reward1.setName("游戏时间");
        
        Reward reward2 = new Reward();
        reward2.setName("零花钱");
        
        RedeemResult result1 = defaultService.redeem(reward1, child);
        RedeemResult result2 = defaultService.redeem(reward2, child);
        
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        assertNull(result1.getNote());
        assertNull(result2.getNote());
    }
}
