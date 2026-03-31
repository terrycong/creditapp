package com.creditapp.service;

import com.creditapp.entity.Child;
import com.creditapp.entity.Reward;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 默认礼物兑换服务
 * 处理普通礼物的兑换逻辑（无特殊业务逻辑）
 */
@Slf4j
@Service
public class DefaultRedeemService implements RedeemableService {
    
    /**
     * 默认支持所有礼物（作为兜底）
     */
    @Override
    public boolean supports(Reward reward) {
        return true;
    }
    
    /**
     * 普通礼物兑换逻辑
     * 无特殊处理，返回空 note
     */
    @Override
    public RedeemResult redeem(Reward reward, Child child) {
        log.info("Redeeming default reward: rewardId={}, rewardName={}, childId={}, childName={}", 
                reward.getId(), reward.getName(), child.getId(), child.getUsername());
        
        // 普通礼物无需特殊逻辑，返回空 note
        return RedeemResult.success(null);
    }
    
    @Override
    public int getOrder() {
        return 1000;  // 默认服务优先级最低（作为兜底）
    }
}
