package com.creditapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 抽奖奖品实体
 * 每个抽奖主题可以有多个奖品，每个奖品有自己的概率/权重
 */
@Entity
@Table(name = "lottery_prizes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotteryPrize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属抽奖主题
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lottery_theme_id", nullable = false)
    private LotteryTheme lotteryTheme;

    /**
     * 奖品名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 奖品描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 奖品价值（积分）
     */
    @Column(name = "prize_value", nullable = false)
    private Integer value;

    /**
     * 概率权重（权重越高，中奖概率越大）
     * 总概率 = 该奖品权重 / 所有奖品权重之和
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer weight = 1;

    /**
     * 固定概率模式下的概率值（0-100 之间的整数，表示百分比）
     * 例如：10 表示 10% 的概率
     */
    @Column
    private Integer probability;

    /**
     * 奖品库存数量（-1 表示无限）
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = -1;

    /**
     * 已兑换数量
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer redeemedCount = 0;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 奖品图片 URL
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * 关联的可兑换礼物 ID（可选）
     * 如果设置了此字段，中奖时会获得实际礼物并创建兑换记录
     */
    @Column(name = "reward_id")
    private Long rewardId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 检查是否有库存
     */
    public boolean hasQuantity() {
        return quantity == -1 || redeemedCount < quantity;
    }

    /**
     * 增加兑换计数
     */
    public void incrementRedeemed() {
        this.redeemedCount++;
    }
}
