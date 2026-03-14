package com.creditapp.entity;

/**
 * 抽奖结果状态枚举
 */
public enum DrawResultStatus {
    
    /**
     * 未中奖
     */
    NO_WIN("未中奖"),
    
    /**
     * 中奖（已获得积分）
     */
    WON("中奖"),
    
    /**
     * 特等奖
     */
    JACKPOT("特等奖");
    
    private final String displayName;
    
    DrawResultStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
