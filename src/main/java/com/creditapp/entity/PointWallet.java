package com.creditapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PointWallet Entity
 * Tracks individual batches of points earned by a child with expiration tracking.
 * Each task completion creates a new PointWallet entry.
 * Points are consumed using FIFO (First In, First Out) method.
 */
@Entity
@Table(name = "point_wallet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The child who owns these points
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    /**
     * Original points earned (before any spending)
     */
    @Column(name = "original_points", nullable = false)
    private Integer originalPoints;

    /**
     * Remaining points (after spending)
     */
    @Column(name = "remaining_points", nullable = false)
    private Integer remainingPoints;

    /**
     * Date when points were earned
     */
    @Column(name = "earned_date", nullable = false)
    private LocalDateTime earnedDate;

    /**
     * Expiration date (earned_date + expiration_days)
     */
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    /**
     * Reference to source (task completion, bonus, etc.)
     */
    @Column(name = "source_type", length = 50)
    private String sourceType;

    /**
     * Reference ID (task completion ID, etc.)
     */
    @Column(name = "source_id")
    private Long sourceId;

    /**
     * Whether this batch has been fully spent
     */
    @Column(nullable = false)
    private boolean fullySpent = false;

    /**
     * Whether points have expired
     */
    @Column(nullable = false)
    private boolean expired = false;

    /**
     * Date when points were expired
     */
    @Column(name = "expired_date")
    private LocalDateTime expiredDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (earnedDate == null) {
            earnedDate = LocalDateTime.now();
        }
        if (remainingPoints == null) {
            remainingPoints = originalPoints;
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if these points are expired
     */
    public boolean isExpired() {
        if (expirationDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(expirationDate);
    }

    /**
     * Spend points from this batch
     * @param amount amount to spend
     * @return actual amount spent (may be less than requested if not enough points)
     */
    public int spendPoints(int amount) {
        if (remainingPoints <= 0 || fullySpent) {
            return 0;
        }

        int actualSpend = Math.min(amount, remainingPoints);
        this.remainingPoints -= actualSpend;

        if (this.remainingPoints <= 0) {
            this.fullySpent = true;
        }

        return actualSpend;
    }

    /**
     * Mark as expired and update remaining points
     */
    public void markAsExpired() {
        this.expired = true;
        this.expiredDate = LocalDateTime.now();
        this.remainingPoints = 0;
        this.fullySpent = true;
    }
}
