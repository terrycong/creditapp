package com.creditapp.service;

import com.creditapp.entity.Child;
import com.creditapp.entity.Coupon;
import com.creditapp.entity.Reward;
import com.creditapp.repository.CouponRepository;
import com.creditapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 上网券兑换服务测试
 */
@ExtendWith(MockitoExtension.class)
class CouponRedeemServiceTest {
    
    @Mock
    private CouponRepository couponRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private CouponRedeemService couponRedeemService;
    
    private Reward couponReward;
    private Reward normalReward;
    private Child child;
    private Coupon availableCoupon;
    
    @BeforeEach
    void setUp() {
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
        
        // 创建可用券码
        availableCoupon = new Coupon();
        availableCoupon.setId(1L);
        availableCoupon.setCode("COUPON_TEST_001");
        availableCoupon.setEnabled(true);
        availableCoupon.setRedeemed(false);
        availableCoupon.setTimeoutSeconds(7200); // 2 小时
        availableCoupon.setUsername(null); // 不指定用户
    }
    
    @Test
    void testSupports_CouponReward() {
        assertTrue(couponRedeemService.supports(couponReward));
    }
    
    @Test
    void testSupports_NormalReward() {
        assertFalse(couponRedeemService.supports(normalReward));
    }
    
    @Test
    void testSupports_Keywords() {
        Reward reward1 = new Reward();
        reward1.setName("Internet Coupon");
        assertTrue(couponRedeemService.supports(reward1));
        
        Reward reward2 = new Reward();
        reward2.setName("其他礼物");
        reward2.setDescription("包含上网时长");
        assertTrue(couponRedeemService.supports(reward2));
    }
    
    @Test
    void testRedeem_Success() {
        // 准备数据
        List<Coupon> availableCoupons = Arrays.asList(availableCoupon);
        when(couponRepository.findByEnabledTrueAndRedeemedFalse()).thenReturn(availableCoupons);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon saved = invocation.getArgument(0);
            saved.setRedeemedAt(LocalDateTime.now()); // Set redeemedAt when saved
            return saved;
        });
        
        // 执行兑换
        RedeemResult result = couponRedeemService.redeem(couponReward, child);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getNote());
        assertTrue(result.getNote().contains("COUPON_TEST_001"));
        assertTrue(result.getNote().contains("小时"));
        
        // 验证券码状态更新
        assertTrue(availableCoupon.getRedeemed());
        assertEquals(child, availableCoupon.getRedeemedBy());
        assertEquals(1, availableCoupon.getUsedCount());
        
        verify(couponRepository).save(availableCoupon);
    }
    
    @Test
    void testRedeem_NoAvailableCoupons() {
        // 没有可用券码
        when(couponRepository.findByEnabledTrueAndRedeemedFalse()).thenReturn(Arrays.asList());
        
        // 执行兑换
        RedeemResult result = couponRedeemService.redeem(couponReward, child);
        
        // 验证失败
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("暂无可用上网券，请联系家长补充券码", result.getErrorMessage());
        
        verify(couponRepository, never()).save(any());
    }
    
    @Test
    void testRedeem_WithUsername() {
        // 券码有登录账号（不影响兑换）
        availableCoupon.setUsername("login_account_001");
        List<Coupon> availableCoupons = Arrays.asList(availableCoupon);
        when(couponRepository.findByEnabledTrueAndRedeemedFalse()).thenReturn(availableCoupons);
        when(couponRepository.save(any(Coupon.class))).thenReturn(availableCoupon);
        
        // 执行兑换
        RedeemResult result = couponRedeemService.redeem(couponReward, child);
        
        // 验证成功
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getNote().contains("COUPON_TEST_001"));
        assertTrue(result.getNote().contains("登录账号：login_account_001"));
        
        verify(couponRepository).save(availableCoupon);
    }
    
    @Test
    void testRedeem_NoteFormat() {
        // 设置券码登录账号和备注
        availableCoupon.setUsername("login_user_001");
        availableCoupon.setComment("测试备注");
        List<Coupon> availableCoupons = Arrays.asList(availableCoupon);
        when(couponRepository.findByEnabledTrueAndRedeemedFalse()).thenReturn(availableCoupons);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon saved = invocation.getArgument(0);
            saved.setRedeemedAt(LocalDateTime.now());
            return saved;
        });
        
        // 执行兑换
        RedeemResult result = couponRedeemService.redeem(couponReward, child);
        
        // 验证 note 格式
        assertNotNull(result.getNote());
        assertTrue(result.getNote().contains("【上网券】"));
        assertTrue(result.getNote().contains("COUPON_TEST_001"));
        assertTrue(result.getNote().contains("登录账号：login_user_001"));
        assertTrue(result.getNote().contains("小时"));
        assertTrue(result.getNote().contains("兑换时间："));
        assertTrue(result.getNote().contains("测试备注"));
    }
    
    @Test
    void testGetOrder() {
        assertEquals(10, couponRedeemService.getOrder());
    }
    
    @Test
    void testRedeem_MatchDuration() {
        // 创建 2 小时奖励
        Reward twoHourReward = new Reward();
        twoHourReward.setId(1L);
        twoHourReward.setName("2 小时上网券");
        twoHourReward.setPointsRequired(100);
        
        // 准备券码：1 小时、2 小时、30 分钟
        Coupon coupon1h = new Coupon();
        coupon1h.setId(1L);
        coupon1h.setCode("COUPON_1H");
        coupon1h.setEnabled(true);
        coupon1h.setRedeemed(false);
        coupon1h.setTimeoutSeconds(3600); // 1 小时
        
        Coupon coupon2h = new Coupon();
        coupon2h.setId(2L);
        coupon2h.setCode("COUPON_2H");
        coupon2h.setEnabled(true);
        coupon2h.setRedeemed(false);
        coupon2h.setTimeoutSeconds(7200); // 2 小时
        
        Coupon coupon30m = new Coupon();
        coupon30m.setId(3L);
        coupon30m.setCode("COUPON_30M");
        coupon30m.setEnabled(true);
        coupon30m.setRedeemed(false);
        coupon30m.setTimeoutSeconds(1800); // 30 分钟
        
        List<Coupon> availableCoupons = Arrays.asList(coupon1h, coupon2h, coupon30m);
        when(couponRepository.findByEnabledTrueAndRedeemedFalse()).thenReturn(availableCoupons);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon saved = invocation.getArgument(0);
            saved.setRedeemedAt(LocalDateTime.now());
            return saved;
        });
        
        // 执行兑换
        RedeemResult result = couponRedeemService.redeem(twoHourReward, child);
        
        // 验证应该选择 2 小时的券码
        assertNotNull(result);
        assertTrue(result.isSuccess());
        // 验证选择了正确的券码（timeout=7200 秒）
        assertTrue(result.getNote().contains("COUPON_2H") || result.getNote().contains("2 小时"));
    }
    
    @Test
    void testRedeem_NoMatchingDuration() {
        // 创建 3 小时奖励
        Reward threeHourReward = new Reward();
        threeHourReward.setId(1L);
        threeHourReward.setName("3 小时上网券");
        threeHourReward.setPointsRequired(150);
        
        // 只有 1 小时和 2 小时的券码
        Coupon coupon1h = new Coupon();
        coupon1h.setId(1L);
        coupon1h.setCode("COUPON_1H");
        coupon1h.setEnabled(true);
        coupon1h.setRedeemed(false);
        coupon1h.setTimeoutSeconds(3600);
        
        Coupon coupon2h = new Coupon();
        coupon2h.setId(2L);
        coupon2h.setCode("COUPON_2H");
        coupon2h.setEnabled(true);
        coupon2h.setRedeemed(false);
        coupon2h.setTimeoutSeconds(7200);
        
        List<Coupon> availableCoupons = Arrays.asList(coupon1h, coupon2h);
        when(couponRepository.findByEnabledTrueAndRedeemedFalse()).thenReturn(availableCoupons);
        
        // 执行兑换
        RedeemResult result = couponRedeemService.redeem(threeHourReward, child);
        
        // 验证失败
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("180"));  // 3 小时=180 分钟
        
        verify(couponRepository, never()).save(any());
    }
}
