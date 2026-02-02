package com.creditapp.dto;

import com.creditapp.entity.TaskDeadlineType;
import com.creditapp.entity.TaskType;
import com.creditapp.entity.TaskStatus;

public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Integer points;
    private TaskType type;
    private TaskStatus status;
    private Long createdById;
    private String createdByName;
    private Long assignedChildId;
    private String assignedChildName;
    private boolean active;

    // 强制任务相关字段
    private TaskDeadlineType deadlineType;
    private Integer deadlineValue;
    private Integer penaltyPoints;

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
        private Long assignedChildId;
        private String assignedChildName;
        private boolean active;
        private TaskDeadlineType deadlineType;
        private Integer deadlineValue;
        private Integer penaltyPoints;

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

        public Builder assignedChildId(Long assignedChildId) {
            this.assignedChildId = assignedChildId;
            return this;
        }

        public Builder assignedChildName(String assignedChildName) {
            this.assignedChildName = assignedChildName;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
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
            dto.assignedChildId = this.assignedChildId;
            dto.assignedChildName = this.assignedChildName;
            dto.active = this.active;
            dto.deadlineType = this.deadlineType;
            dto.deadlineValue = this.deadlineValue;
            dto.penaltyPoints = this.penaltyPoints;
            return dto;
        }
    }
}
