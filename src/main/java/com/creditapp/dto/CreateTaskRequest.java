package com.creditapp.dto;

import com.creditapp.entity.TaskType;
import jakarta.validation.constraints.*;

public class CreateTaskRequest {
    @NotBlank(message = "任务标题不能为空")
    @Size(max = 100, message = "任务标题不能超过100字符")
    private String title;

    @Size(max = 500, message = "任务描述不能超过500字符")
    private String description;

    @NotNull(message = "任务积分不能为空")
    @Min(value = 1, message = "任务积分至少为1")
    private Integer points;

    @NotNull(message = "任务类型不能为空")
    private TaskType type;

    private Long assignedChildId;

    // Getters and Setters
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

    public Long getAssignedChildId() {
        return assignedChildId;
    }

    public void setAssignedChildId(Long assignedChildId) {
        this.assignedChildId = assignedChildId;
    }
}
