package com.creditapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽奖记录实体
 * 记录每次抽奖的详细信息
 */
@Entity
@Table(name = "lottery_draws")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotteryDraw {

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
     * 参与抽奖的小孩
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    /**
     * 抽奖消耗的积分
     */
    @Column(nullable = false)
    private Integer pointsCost;

    /**
     * 抽奖结果状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DrawResultStatus resultStatus;

    /**
     * 中奖的奖品列表（可能中多个奖品）
     */
    @OneToMany(mappedBy = "lotteryDraw", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LotteryDrawResult> drawResults = new ArrayList<>();

    /**
     * 抽奖时间
     */
    @Column(nullable = false)
    private LocalDateTime drawAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (drawAt == null) {
            drawAt = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
