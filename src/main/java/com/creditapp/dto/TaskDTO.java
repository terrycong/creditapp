package com.creditapp.dto;

import com.creditapp.entity.TaskDeadlineType;
import com.creditapp.entity.TaskType;
import com.creditapp.entity.TaskStatus;
import java.time.LocalDateTime;

public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Integer points;
    private TaskType type;
    private TaskStatus status;
    private Long createdById;
    private String createdByName;
    private boolean active;
    
    // 创建时间
    private LocalDateTime createdAt;

    // 强制任务相关字段
    private TaskDeadlineType deadlineType;
    private Integer deadlineValue;
    private Integer penaltyPoints;

    // 任务领取信息（用于家长市场页面显示）
    private String pickedByChildName;

    // 任务分配信息（用于家长任务列表显示）
    private Long assignedChildId;
    private String assignedChildName;

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

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public String getPickedByChildName() {
        return pickedByChildName;
    }

    public void setPickedByChildName(String pickedByChildName) {
        this.pickedByChildName = pickedByChildName;
    }

    public Long getAssignedChildId() {
        return assignedChildId;
    }

    public void setAssignedChildId(Long assignedChildId) {
        this.assignedChildId = assignedChildId;
    }

    public String getAssignedChildName() {
        return assignedChildName;
    }

    public void setAssignedChildName(String assignedChildName) {
        this.assignedChildName = assignedChildName;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private Integer points;
        private TaskType type;
        private TaskStatus status;
        private Long createdById;
        private String createdByName;
        private boolean active;
        private LocalDateTime createdAt;
        private TaskDeadlineType deadlineType;
        private Integer deadlineValue;
        private Integer penaltyPoints;
        private String pickedByChildName;
        private Long assignedChildId;
        private String assignedChildName;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder points(Integer points) {
            this.points = points;
            return this;
        }

        public Builder type(TaskType type) {
            this.type = type;
            return this;
        }

        public Builder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdById(Long createdById) {
            this.createdById = createdById;
            return this;
        }

        public Builder createdByName(String createdByName) {
            this.createdByName = createdByName;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder deadlineType(TaskDeadlineType deadlineType) {
            this.deadlineType = deadlineType;
            return this;
        }

        public Builder deadlineValue(Integer deadlineValue) {
            this.deadlineValue = deadlineValue;
            return this;
        }

        public Builder penaltyPoints(Integer penaltyPoints) {
            this.penaltyPoints = penaltyPoints;
            return this;
        }

        public Builder pickedByChildName(String pickedByChildName) {
            this.pickedByChildName = pickedByChildName;
            return this;
        }

        public Builder assignedChildId(Long assignedChildId) {
            this.assignedChildId = assignedChildId;
            return this;
        }

        public Builder assignedChildName(String assignedChildName) {
            this.assignedChildName = assignedChildName;
            return this;
        }

        public TaskDTO build() {
            TaskDTO dto = new TaskDTO();
            dto.id = this.id;
            dto.title = this.title;
            dto.description = this.description;
            dto.points = this.points;
            dto.type = this.type;
            dto.status = this.status;
            dto.createdById = this.createdById;
            dto.createdByName = this.createdByName;
            dto.active = this.active;
            dto.createdAt = this.createdAt;
            dto.deadlineType = this.deadlineType;
            dto.deadlineValue = this.deadlineValue;
            dto.penaltyPoints = this.penaltyPoints;
            dto.pickedByChildName = this.pickedByChildName;
            dto.assignedChildId = this.assignedChildId;
            dto.assignedChildName = this.assignedChildName;
            return dto;
        }
    }
}
