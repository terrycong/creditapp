package com.creditapp.dto;

public class RewardDTO {
    private Long id;
    private String name;
    private String description;
    private Integer quantity;
    private Integer pointsRequired;
    private String imageUrl;
    private boolean active;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(Integer pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String description;
        private Integer quantity;
        private Integer pointsRequired;
        private String imageUrl;
        private boolean active;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder pointsRequired(Integer pointsRequired) {
            this.pointsRequired = pointsRequired;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public RewardDTO build() {
            RewardDTO dto = new RewardDTO();
            dto.id = this.id;
            dto.name = this.name;
            dto.description = this.description;
            dto.quantity = this.quantity;
            dto.pointsRequired = this.pointsRequired;
            dto.imageUrl = this.imageUrl;
            dto.active = this.active;
            return dto;
        }
    }
}
