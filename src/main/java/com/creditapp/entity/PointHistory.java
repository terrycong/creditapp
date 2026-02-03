package com.creditapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * PointHistory Entity
 * Tracks all point changes for a child including:
 * - Original points before change
 * - Change amount (positive for earning, negative for spending/penalty)
 * - Resulting points after change
 */
@Entity
@Table(name = "point_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The child whose points changed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    /**
     * Points before this change
     */
    @Column(name = "original_points", nullable = false)
    private Integer originalPoints;

    /**
     * Change amount (positive = earned, negative = spent/penalty)
     */
    @Column(name = "change_points", nullable = false)
    private Integer changePoints;

    /**
     * Points after this change
     */
    @Column(name = "after_points", nullable = false)
    private Integer afterPoints;

    /**
     * Reason for point change
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 30)
    private PointChangeType changeType;

    /**
     * Optional description or note
     */
    @Column(length = 500)
    private String description;

    /**
     * Reference to related entity (task completion, reward redemption, etc.)
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * Type of reference (TASK_COMPLETION, REWARD_REDEMPTION, MANUAL_ADJUSTMENT, PENALTY)
     */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * Who made the change (system, parent, etc.)
     */
    @Column(name = "changed_by_id")
    private Long changedById;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // Calculate afterPoints if not set
        if (afterPoints == null && originalPoints != null && changePoints != null) {
            afterPoints = originalPoints + changePoints;
        }
    }

    /**
     * Get formatted change display (e.g., "+10" or "-5")
     */
    public String getFormattedChange() {
        if (changePoints >= 0) {
            return "+" + changePoints;
        }
        return String.valueOf(changePoints);
    }

    /**
     * Check if this is an earning (positive change)
     */
    public boolean isEarning() {
        return changePoints > 0;
    }

    /**
     * Check if this is a spending/penalty (negative change)
     */
    public boolean isSpending() {
        return changePoints < 0;
    }
}
