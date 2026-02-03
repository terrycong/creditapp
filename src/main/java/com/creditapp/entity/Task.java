package com.creditapp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false)
    private boolean active = true;

    // Marketplace task fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_child_id")
    private Child assignedChild;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picked_by_child_id")
    private Child pickedByChild;

    // 强制任务相关字段
    @Enumerated(EnumType.STRING)
    @Column(name = "deadline_type")
    private TaskDeadlineType deadlineType;  // 时间限制类型：DAILY, WEEKLY_TIMES

    @Column(name = "deadline_value")
    private Integer deadlineValue;  // 时间限制值（如一周3次）

    @Column(name = "penalty_points")
    private Integer penaltyPoints;  // 未完成惩罚积分

    // Constructors
    public Task() {}

    public Task(Long id, String title, String description, Integer points, TaskType type, TaskStatus status,
                User createdBy, boolean active) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.points = points;
        this.type = type;
        this.status = status;
        this.createdBy = createdBy;
        this.active = active;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public TaskDeadlineType getDeadlineType() {
        return deadlineType;
    }

    public void setDeadlineType(TaskDeadlineType deadlineType) {
        this.deadlineType = deadlineType;
    }

    public Integer getDeadlineValue() {
        return deadlineValue;
    }

    public void setDeadlineValue(Integer deadlineValue) {
        this.deadlineValue = deadlineValue;
    }

    public Integer getPenaltyPoints() {
        return penaltyPoints;
    }

    public void setPenaltyPoints(Integer penaltyPoints) {
        this.penaltyPoints = penaltyPoints;
    }

    public Child getAssignedChild() {
        return assignedChild;
    }

    public void setAssignedChild(Child assignedChild) {
        this.assignedChild = assignedChild;
    }

    public Child getPickedByChild() {
        return pickedByChild;
    }

    public void setPickedByChild(Child pickedByChild) {
        this.pickedByChild = pickedByChild;
    }
}
