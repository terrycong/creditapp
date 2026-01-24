package com.creditapp.dto;

import java.time.LocalDateTime;

public class RewardRedemptionDTO {
    private Long id;
    private Long rewardId;
    private String rewardName;
    private Integer pointsRequired;
    private Long childId;
    private String childName;
    private LocalDateTime redeemedAt;
    private String note;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRewardId() {
        return rewardId;
    }

    public void setRewardId(Long rewardId) {
        this.rewardId = rewardId;
    }

    public String getRewardName() {
        return rewardName;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public Integer getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(Integer pointsRequired) {
        this.pointsRequired = pointsRequired;
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

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(LocalDateTime redeemedAt) {
        this.redeemedAt = redeemedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long rewardId;
        private String rewardName;
        private Integer pointsRequired;
        private Long childId;
        private String childName;
        private LocalDateTime redeemedAt;
        private String note;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder rewardId(Long rewardId) {
            this.rewardId = rewardId;
            return this;
        }

        public Builder rewardName(String rewardName) {
            this.rewardName = rewardName;
            return this;
        }

        public Builder pointsRequired(Integer pointsRequired) {
            this.pointsRequired = pointsRequired;
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

        public Builder redeemedAt(LocalDateTime redeemedAt) {
            this.redeemedAt = redeemedAt;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public RewardRedemptionDTO build() {
            RewardRedemptionDTO dto = new RewardRedemptionDTO();
            dto.id = this.id;
            dto.rewardId = this.rewardId;
            dto.rewardName = this.rewardName;
            dto.pointsRequired = this.pointsRequired;
            dto.childId = this.childId;
            dto.childName = this.childName;
            dto.redeemedAt = this.redeemedAt;
            dto.note = this.note;
            return dto;
        }
    }
}
