package com.creditapp.dto;

import com.creditapp.entity.CompletionStatus;

import java.time.LocalDateTime;

public class TaskCompletionDTO {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private Integer taskPoints;
    private Long childId;
    private String childName;
    private CompletionStatus status;
    private String proof;
    private LocalDateTime completedAt;
    private LocalDateTime approvedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public Integer getTaskPoints() {
        return taskPoints;
    }

    public void setTaskPoints(Integer taskPoints) {
        this.taskPoints = taskPoints;
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public CompletionStatus getStatus() {
        return status;
    }

    public void setStatus(CompletionStatus status) {
        this.status = status;
    }

    public String getProof() {
        return proof;
    }

    public void setProof(String proof) {
        this.proof = proof;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long taskId;
        private String taskTitle;
        private Integer taskPoints;
        private Long childId;
        private String childName;
        private CompletionStatus status;
        private String proof;
        private LocalDateTime completedAt;
        private LocalDateTime approvedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder taskId(Long taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder taskTitle(String taskTitle) {
            this.taskTitle = taskTitle;
            return this;
        }

        public Builder taskPoints(Integer taskPoints) {
            this.taskPoints = taskPoints;
            return this;
        }

        public Builder childId(Long childId) {
            this.childId = childId;
            return this;
        }

        public Builder childName(String childName) {
            this.childName = childName;
            return this;
        }

        public Builder status(CompletionStatus status) {
            this.status = status;
            return this;
        }

        public Builder proof(String proof) {
            this.proof = proof;
            return this;
        }

        public Builder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder approvedAt(LocalDateTime approvedAt) {
            this.approvedAt = approvedAt;
            return this;
        }

        public TaskCompletionDTO build() {
            TaskCompletionDTO dto = new TaskCompletionDTO();
            dto.id = this.id;
            dto.taskId = this.taskId;
            dto.taskTitle = this.taskTitle;
            dto.taskPoints = this.taskPoints;
            dto.childId = this.childId;
            dto.childName = this.childName;
            dto.status = this.status;
            dto.proof = this.proof;
            dto.completedAt = this.completedAt;
            dto.approvedAt = this.approvedAt;
            return dto;
        }
    }
}
