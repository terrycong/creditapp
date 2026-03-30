package com.creditapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽奖主题实体
 * 家长可以创建多个抽奖主题，每个主题有自己的奖品池和消耗积分
 */
@Entity
@Table(name = "lottery_themes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotteryTheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 抽奖主题名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 抽奖主题描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 每次抽奖消耗的积分
     */
    @Column(nullable = false)
    private Integer pointsPerDraw;

    /**
     * 抽奖类型（固定概率、动态概率等）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LotteryTypeEnum type;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * 创建者（家长）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    /**
     * 奖品列表
     */
    @OneToMany(mappedBy = "lotteryTheme", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LotteryPrize> prizes = new ArrayList<>();

    /**
     * 抽奖记录
     */
    @OneToMany(mappedBy = "lotteryTheme", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LotteryDraw> lotteryDraws = new ArrayList<>();

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
