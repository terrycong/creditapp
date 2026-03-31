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
     * 1. 从奖励名称/描述解析期望时长（分钟）
     * 2. 查找匹配时长的可用券码
     * 3. 标记为已兑换
     * 4. 返回券码详情
     */
    @Override
    @Transactional
    public RedeemResult redeem(Reward reward, Child child) {
        log.info(" redeeming coupon reward: rewardId={}, rewardName={}, childId={}, childName={}", 
                reward.getId(), reward.getName(), child.getId(), child.getUsername());
        
        // 解析期望时长（分钟）
        Integer expectedMinutes = parseExpectedMinutes(reward);
        log.info("Expected duration: {} minutes", expectedMinutes);
        
        // 查找可用的券码（未兑换、已启用）
        List<Coupon> availableCoupons = couponRepository.findByEnabledTrueAndRedeemedFalse();
        
        if (availableCoupons.isEmpty()) {
            log.warn("No available coupons in pool");
            return RedeemResult.failure("暂无可用上网券，请联系家长补充券码");
        }
        
        // 如果解析出期望时长，过滤匹配的券码
        List<Coupon> matchingCoupons;
        if (expectedMinutes != null) {
            int expectedSeconds = expectedMinutes * 60;
            matchingCoupons = availableCoupons.stream()
                    .filter(c -> c.getTimeoutSeconds() == expectedSeconds)
                    .collect(java.util.stream.Collectors.toList());
            
            log.info("Found {} matching coupons (expected {}s)", matchingCoupons.size(), expectedSeconds);
            
            if (matchingCoupons.isEmpty()) {
                log.warn("No coupons match the expected duration of {} minutes", expectedMinutes);
                return RedeemResult.failure("暂无" + expectedMinutes + "分钟时长的上网券，请联系家长补充券码");
            }
        } else {
            // 没有指定期望时长，使用所有可用券码
            matchingCoupons = availableCoupons;
            log.info("No specific duration required, using all {} available coupons", matchingCoupons.size());
        }
        
        // 随机选择一个匹配的券码
        Coupon selectedCoupon = matchingCoupons.get((int) (Math.random() * matchingCoupons.size()));
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
    
    /**
     * 从奖励名称和描述中解析期望时长（分钟）
     * 支持格式：
     * - "2 小时" → 120 分钟
     * - "30 分钟" → 30 分钟
     * - "1 小时 30 分钟" → 90 分钟
     * - "120 分钟" → 120 分钟
     */
    private Integer parseExpectedMinutes(Reward reward) {
        if (reward == null) {
            return null;
        }
        
        String text = "";
        
        // 合并名称和描述
        if (reward.getName() != null) {
            text += reward.getName().toLowerCase();
        }
        if (reward.getDescription() != null) {
            text += " " + reward.getDescription().toLowerCase();
        }
        
        if (text.isEmpty()) {
            return null;
        }
        
        // 尝试匹配"X 小时 Y 分钟"格式
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*小\\s*时 [\\s]*(\\d+)?\\s*分\\s*钟？");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            int hours = Integer.parseInt(matcher.group(1));
            int minutes = 0;
            if (matcher.group(2) != null) {
                minutes = Integer.parseInt(matcher.group(2));
            }
            return hours * 60 + minutes;
        }
        
        // 尝试匹配"X 小时"格式
        pattern = java.util.regex.Pattern.compile("(\\d+)\\s*小\\s*时");
        matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            int hours = Integer.parseInt(matcher.group(1));
            return hours * 60;
        }
        
        // 尝试匹配"X 分钟"格式
        pattern = java.util.regex.Pattern.compile("(\\d+)\\s*分\\s*钟？");
        matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        
        // 没有匹配到时长信息
        return null;
    }
}
