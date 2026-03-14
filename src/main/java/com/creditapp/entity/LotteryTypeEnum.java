package com.creditapp.entity;

/**
 * 抽奖类型枚举
 */
public enum LotteryTypeEnum {
    
    /**
     * 固定概率 - 每个奖品概率固定
     */
    FIXED_PROBABILITY("固定概率"),
    
    /**
     * 权重随机 - 根据权重计算概率
     */
    WEIGHTED_RANDOM("权重随机"),
    
    /**
     * 必中模式 - 100% 中奖，从奖品池中随机
     */
    GUARANTEED("必中模式");
    
    private final String displayName;
    
    LotteryTypeEnum(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
