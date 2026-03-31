package com.creditapp.service;

import com.creditapp.entity.Child;
import com.creditapp.entity.Reward;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 兑换策略管理器
 * 管理所有 RedeemableService 实现，选择合适的服务处理兑换
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedemptionStrategyManager {
    
    private final List<RedeemableService> redeemableServices;
    
    /**
     * 选择合适的服务并执行兑换
     * @param reward 礼物对象
     * @param child 小孩对象
     * @return 兑换结果
     */
    public RedeemResult redeem(Reward reward, Child child) {
        log.debug("Finding suitable redeem service for reward: {}", reward.getName());
        
        // 按优先级排序，找到第一个支持的服务
        RedeemableService selectedService = redeemableServices.stream()
                .sorted((a, b) -> Integer.compare(a.getOrder(), b.getOrder()))
                .filter(service -> service.supports(reward))
                .findFirst()
                .orElse(null);
        
        if (selectedService == null) {
            log.warn("No suitable redeem service found for reward: {}", reward.getName());
            return RedeemResult.failure("不支持的礼物类型");
        }
        
        log.debug("Selected redeem service: {}", selectedService.getClass().getSimpleName());
        return selectedService.redeem(reward, child);
    }
}
