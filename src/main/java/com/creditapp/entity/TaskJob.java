package com.creditapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * TaskJob Entity
 * Represents a task assignment/instance linking a Child to a Task.
 * Contains snapshot of task data at assignment time for data consistency.
 */
@Entity
@Table(name = "task_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the original task definition (static catalog)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /**
     * The child this task is assigned to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    /**
     * Current status of this task job
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    // ==================== SNAPSHOT FIELDS ====================
    // Copied from Task at assignment time to ensure data consistency
    // Even if the original Task is modified, TaskJob preserves the original values

    @Column(nullable = false, length = 100)
    private String snapshotTitle;

    @Column(length = 500)
    private String snapshotDescription;

    @Column(nullable = false)
    private Integer snapshotPoints;

    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_task_type", nullable = false, length = 20)
    private TaskType snapshotTaskType;

    // ==================== TIMELINE FIELDS ====================

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (assignedAt == null) {
            assignedAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        // Copy snapshot data from task if not already set
        if (task != null && snapshotTitle == null) {
            snapshotTitle = task.getTitle();
            snapshotDescription = task.getDescription();
            snapshotPoints = task.getPoints();
            snapshotTaskType = task.getType();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== CONVENIENCE METHODS ====================

    /**
     * Mark this job as started
     */
    public void start() {
        if (this.status != JobStatus.ASSIGNED) {
            throw new IllegalStateException("Can only start an ASSIGNED job");
        }
        this.status = JobStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Check if this job is active (can be worked on)
     */
    public boolean isActive() {
        return this.status == JobStatus.ASSIGNED || this.status == JobStatus.IN_PROGRESS;
    }

    /**
     * Check if this job is completed (submitted for approval)
     */
    public boolean isCompleted() {
        return this.status == JobStatus.COMPLETED;
    }

    /**
     * Check if this job is cancelled
     */
    public boolean isCancelled() {
        return this.status == JobStatus.CANCELLED;
    }

    /**
     * Get the points for this job (from snapshot)
     */
    public Integer getPoints() {
        return snapshotPoints;
    }

    /**
     * Get the task type (from snapshot)
     */
    public TaskType getTaskType() {
        return snapshotTaskType;
    }

    /**
     * Get the task title (from snapshot)
     */
    public String getTitle() {
        return snapshotTitle;
    }

    /**
     * Get the task description (from snapshot)
     */
    public String getDescription() {
        return snapshotDescription;
    }
}
