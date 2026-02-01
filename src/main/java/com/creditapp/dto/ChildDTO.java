package com.creditapp.dto;

import com.creditapp.entity.UserRole;

public class ChildDTO {
    private Long id;
    private String username;
    private UserRole role;
    private Integer points;
    private Long parentId;
    private String parentName;
    private Integer activeTaskCount;
    private Integer completedTaskCount;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Integer getActiveTaskCount() {
        return activeTaskCount;
    }

    public void setActiveTaskCount(Integer activeTaskCount) {
        this.activeTaskCount = activeTaskCount;
    }

    public Integer getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(Integer completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String username;
        private UserRole role;
        private Integer points;
        private Long parentId;
        private String parentName;
        private Integer activeTaskCount;
        private Integer completedTaskCount;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder role(UserRole role) {
            this.role = role;
            return this;
        }

        public Builder points(Integer points) {
            this.points = points;
            return this;
        }

        public Builder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder parentName(String parentName) {
            this.parentName = parentName;
            return this;
        }

        public Builder activeTaskCount(Integer activeTaskCount) {
            this.activeTaskCount = activeTaskCount;
            return this;
        }

        public Builder completedTaskCount(Integer completedTaskCount) {
            this.completedTaskCount = completedTaskCount;
            return this;
        }

        public ChildDTO build() {
            ChildDTO dto = new ChildDTO();
            dto.id = this.id;
            dto.username = this.username;
            dto.role = this.role;
            dto.points = this.points;
            dto.parentId = this.parentId;
            dto.parentName = this.parentName;
            dto.activeTaskCount = this.activeTaskCount;
            dto.completedTaskCount = this.completedTaskCount;
            return dto;
        }
    }
}
