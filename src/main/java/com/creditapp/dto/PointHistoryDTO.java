package com.creditapp.dto;

import com.creditapp.entity.PointChangeType;

import java.time.LocalDateTime;

/**
 * DTO for PointHistory display
 */
public class PointHistoryDTO {
    private Long id;
    private Long childId;
    private Integer originalPoints;
    private Integer changePoints;
    private Integer afterPoints;
    private PointChangeType changeType;
    private String changeTypeName;
    private String description;
    private Long referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
    private String formattedChange;
    private boolean isEarning;
    private boolean isSpending;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
    }

    public Integer getOriginalPoints() {
        return originalPoints;
    }

    public void setOriginalPoints(Integer originalPoints) {
        this.originalPoints = originalPoints;
    }

    public Integer getChangePoints() {
        return changePoints;
    }

    public void setChangePoints(Integer changePoints) {
        this.changePoints = changePoints;
    }

    public Integer getAfterPoints() {
        return afterPoints;
    }

    public void setAfterPoints(Integer afterPoints) {
        this.afterPoints = afterPoints;
    }

    public PointChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(PointChangeType changeType) {
        this.changeType = changeType;
    }

    public String getChangeTypeName() {
        return changeTypeName;
    }

    public void setChangeTypeName(String changeTypeName) {
        this.changeTypeName = changeTypeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedChange() {
        return formattedChange;
    }

    public void setFormattedChange(String formattedChange) {
        this.formattedChange = formattedChange;
    }

    public boolean isEarning() {
        return isEarning;
    }

    public void setEarning(boolean earning) {
        isEarning = earning;
    }

    public boolean isSpending() {
        return isSpending;
    }

    public void setSpending(boolean spending) {
        isSpending = spending;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PointHistoryDTO dto = new PointHistoryDTO();

        public Builder id(Long id) {
            dto.id = id;
            return this;
        }

        public Builder childId(Long childId) {
            dto.childId = childId;
            return this;
        }

        public Builder originalPoints(Integer originalPoints) {
            dto.originalPoints = originalPoints;
            return this;
        }

        public Builder changePoints(Integer changePoints) {
            dto.changePoints = changePoints;
            return this;
        }

        public Builder afterPoints(Integer afterPoints) {
            dto.afterPoints = afterPoints;
            return this;
        }

        public Builder changeType(PointChangeType changeType) {
            dto.changeType = changeType;
            return this;
        }

        public Builder changeTypeName(String changeTypeName) {
            dto.changeTypeName = changeTypeName;
            return this;
        }

        public Builder description(String description) {
            dto.description = description;
            return this;
        }

        public Builder referenceId(Long referenceId) {
            dto.referenceId = referenceId;
            return this;
        }

        public Builder referenceType(String referenceType) {
            dto.referenceType = referenceType;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            dto.createdAt = createdAt;
            return this;
        }

        public Builder formattedChange(String formattedChange) {
            dto.formattedChange = formattedChange;
            return this;
        }

        public Builder isEarning(boolean isEarning) {
            dto.isEarning = isEarning;
            return this;
        }

        public Builder isSpending(boolean isSpending) {
            dto.isSpending = isSpending;
            return this;
        }

        public PointHistoryDTO build() {
            return dto;
        }
    }
}
