package com.creditapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 抽奖结果详情实体
 * 记录单次抽奖中每个奖品的获得情况
 */
@Entity
@Table(name = "lottery_draw_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotteryDrawResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属抽奖记录
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lottery_draw_id", nullable = false)
    private LotteryDraw lotteryDraw;

    /**
     * 奖品
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prize_id", nullable = false)
    private LotteryPrize prize;

    /**
     * 奖品名称（冗余字段，避免关联查询）
     */
    @Column(nullable = false, length = 100)
    private String prizeName;

    /**
     * 关联的礼物 ID（如果奖品是实物礼物）
     */
    @Column(name = "reward_id")
    private Long rewardId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
