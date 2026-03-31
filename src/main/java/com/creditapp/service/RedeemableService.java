package com.creditapp.service;

import com.creditapp.entity.Child;
import com.creditapp.entity.Reward;

/**
 * 可兑换礼物服务接口
 * 每个具体的礼物类型实现此接口来处理特定的兑换逻辑
 */
public interface RedeemableService {
    
    /**
     * 是否支持该礼物的兑换
     * @param reward 礼物对象
     * @return true 如果此服务处理该类礼物
     */
    boolean supports(Reward reward);
    
    /**
     * 执行具体的兑换逻辑
     * @param reward 礼物对象
     * @param child 兑换的小孩
     * @return 兑换结果（包含 note 等信息）
     */
    RedeemResult redeem(Reward reward, Child child);
    
    /**
     * 获取服务优先级（数字越小优先级越高）
     * 默认优先级为 100
     */
    default int getOrder() {
        return 100;
    }
}
