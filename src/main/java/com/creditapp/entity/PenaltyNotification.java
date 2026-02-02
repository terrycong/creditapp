package com.creditapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 惩罚通知实体 - 当强制任务未完成时通知家长
 */
@Entity
@Table(name = "penalty_notifications")
public class PenaltyNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @Column(nullable = false)
    private LocalDateTime notificationTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(name = "penalty_points")
    private Integer penaltyPoints;

    @Column(name = "is_penalty_applied")
    private boolean penaltyApplied = false;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "applied_by_id")
    private Long appliedById;

    // Constructors
    public PenaltyNotification() {
        this.notificationTime = LocalDateTime.now();
        this.status = NotificationStatus.PENDING;
    }

    public PenaltyNotification(Task task, Child child, User parent, Integer penaltyPoints) {
        this.task = task;
        this.child = child;
        this.parent = parent;
        this.penaltyPoints = penaltyPoints;
        this.notificationTime = LocalDateTime.now();
        this.status = NotificationStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Child getChild() {
        return child;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    public LocalDateTime getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(LocalDateTime notificationTime) {
        this.notificationTime = notificationTime;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public Integer getPenaltyPoints() {
        return penaltyPoints;
    }

    public void setPenaltyPoints(Integer penaltyPoints) {
        this.penaltyPoints = penaltyPoints;
    }

    public boolean isPenaltyApplied() {
        return penaltyApplied;
    }

    public void setPenaltyApplied(boolean penaltyApplied) {
        this.penaltyApplied = penaltyApplied;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public Long getAppliedById() {
        return appliedById;
    }

    public void setAppliedById(Long appliedById) {
        this.appliedById = appliedById;
    }
}
