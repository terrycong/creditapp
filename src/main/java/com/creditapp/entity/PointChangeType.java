package com.creditapp.entity;

/**
 * Types of point changes that can occur
 */
public enum PointChangeType {
    /**
     * Child earned points from completing a task
     */
    TASK_COMPLETION("完成任务"),

    /**
     * Child redeemed points for a reward
     */
    REWARD_REDEMPTION("兑换礼物"),

    /**
     * Parent manually adjusted points
     */
    MANUAL_ADJUSTMENT("手动调整"),

    /**
     * Penalty for not completing mandatory task
     */
    PENALTY("惩罚扣分"),

    /**
     * Bonus points from parent
     */
    BONUS("奖励加分"),

    /**
     * Initial points when child account created
     */
    INITIAL("初始积分"),

    /**
     * Other/unknown reason
     */
    OTHER("其他");

    private final String displayName;

    PointChangeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
