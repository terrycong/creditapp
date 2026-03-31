package com.creditapp.service;

import com.creditapp.entity.Child;
import com.creditapp.entity.Coupon;
import com.creditapp.entity.Reward;
import com.creditapp.repository.CouponRepository;
import com.creditapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上网券兑换服务
 * 处理上网券类型的礼物兑换逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponRedeemService implements RedeemableService {
    
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    
    /**
     * 判断是否支持上网券兑换
     * 通过礼物名称或描述中的关键词匹配
     */
    @Override
    public boolean supports(Reward reward) {
        if (reward == null || reward.getName() == null) {
            return false;
        }
        
        String name = reward.getName().toLowerCase();
        String description = reward.getDescription() != null ? 
                            reward.getDescription().toLowerCase() : "";
        
        // 匹配关键词：上网券、上网、coupon、internet
        return name.contains("上网券") || 
               name.contains("上网") ||
               name.contains("coupon") ||
               name.contains("internet") ||
               description.contains("上网券") ||
               description.contains("上网时长") ||
               description.contains("timeout");
    }
    
    /**
     * 执行上网券兑换逻辑
     * 1. 从券码池随机分配一个未兑换的券码
     * 2. 标记为已兑换
     * 3. 返回券码详情
     */
    @Override
    @Transactional
    public RedeemResult redeem(Reward reward, Child child) {
        log.info(" redeeming coupon reward: rewardId={}, rewardName={}, childId={}, childName={}", 
                reward.getId(), reward.getName(), child.getId(), child.getUsername());
        
        // 查找可用的券码（未兑换、已启用）
        List<Coupon> availableCoupons = couponRepository.findByEnabledTrueAndRedeemedFalse();
        
        if (availableCoupons.isEmpty()) {
            log.warn("No available coupons in pool");
            return RedeemResult.failure("暂无可用上网券，请联系家长补充券码");
        }
        
        // 随机选择一个券码
        Coupon selectedCoupon = availableCoupons.get((int) (Math.random() * availableCoupons.size()));
        log.info("Selected coupon: code={}, timeout={}s, username={}", 
                selectedCoupon.getCode(), selectedCoupon.getTimeoutSeconds(), selectedCoupon.getUsername());
        
        // 标记为已兑换
        selectedCoupon.setRedeemed(true);
        selectedCoupon.setRedeemedBy(child);
        selectedCoupon.setRedeemedAt(LocalDateTime.now());
        selectedCoupon.setUsedCount(selectedCoupon.getUsedCount() + 1);
        couponRepository.save(selectedCoupon);
        
        // 构建兑换详情（note）
        String note = buildCouponNote(selectedCoupon);
        log.info("Coupon redeemed successfully: code={}, child={}, note={}", 
                selectedCoupon.getCode(), child.getUsername(), note);
        
        return RedeemResult.success(note, selectedCoupon);
    }
    
    /**
     * 尝试其他券码
     */
    /**
     * 构建券码详情字符串
     */
    private String buildCouponNote(Coupon coupon) {
        StringBuilder note = new StringBuilder();
        note.append("【上网券】\n");
        note.append("券码：").append(coupon.getCode()).append("\n");
        
        // 如果有 username，显示为登录账号
        if (coupon.getUsername() != null && !coupon.getUsername().isEmpty()) {
            note.append("登录账号：").append(coupon.getUsername()).append("\n");
        }
        
        int hours = coupon.getTimeoutSeconds() / 3600;
        int minutes = (coupon.getTimeoutSeconds() % 3600) / 60;
        
        if (hours > 0) {
            note.append("时长：").append(hours).append("小时");
            if (minutes > 0) {
                note.append(minutes).append("分钟");
            }
        } else {
            note.append("时长：").append(minutes).append("分钟");
        }
        
        note.append("\n");
        note.append("兑换时间：").append(coupon.getRedeemedAt());
        
        if (coupon.getComment() != null && !coupon.getComment().isEmpty()) {
            note.append("\n备注：").append(coupon.getComment());
        }
        
        return note.toString();
    }
    
    @Override
    public int getOrder() {
        return 10;  // 上网券服务优先级较高
    }
}
