package com.creditapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 孩子反馈实体
 * 用于收集孩子对系统的功能性和非功能性反馈
 */
@Data
@Entity
@Table(name = "feedbacks", indexes = {
    @Index(name = "idx_feedbacks_child", columnList = "child_id"),
    @Index(name = "idx_feedbacks_status", columnList = "status"),
    @Index(name = "idx_feedbacks_category", columnList = "category"),
    @Index(name = "idx_feedbacks_created", columnList = "created_at")
})
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 提交反馈的孩子
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Child child;

    /**
     * 反馈类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    /**
     * 反馈标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 反馈详细描述
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * 反馈状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    /**
     * 家长回复内容
     */
    @Column(columnDefinition = "TEXT")
    private String parentResponse;

    /**
     * 回复的家长
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User respondedBy;

    /**
     * 回复时间
     */
    private LocalDateTime respondedAt;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * 反馈类型枚举
     */
    public enum Category {
        FUNCTIONAL("功能建议"),
        NON_FUNCTIONAL("非功能建议"),
        OTHER("其他");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 反馈状态枚举
     */
    public enum Status {
        PENDING("待审核"),
        REVIEWED("已查看"),
        ACCEPTED("已采纳"),
        REJECTED("已拒绝");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
