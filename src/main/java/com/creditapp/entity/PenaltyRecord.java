package com.creditapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 违规扣分记录 - 记录已应用的惩罚
 */
@Entity
@Table(name = "penalty_records")
public class PenaltyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "penalty_rule_id", nullable = false)
    private PenaltyRule penaltyRule;

    @Column(nullable = false)
    private Integer points;

    @Column(length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applied_by_id", nullable = false)
    private User appliedBy;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Child getChild() {
        return child;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    public PenaltyRule getPenaltyRule() {
        return penaltyRule;
    }

    public void setPenaltyRule(PenaltyRule penaltyRule) {
        this.penaltyRule = penaltyRule;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public User getAppliedBy() {
        return appliedBy;
    }

    public void setAppliedBy(User appliedBy) {
        this.appliedBy = appliedBy;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
