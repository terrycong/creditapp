package com.creditapp.entity;

/**
 * Task Job Status Enum
 * Represents the lifecycle status of a task assignment/instance for a child.
 */
public enum JobStatus {
    /**
     * Task has been assigned to or picked by a child, but not yet started
     */
    ASSIGNED,

    /**
     * Child has started working on the task
     */
    IN_PROGRESS,

    /**
     * Child has submitted the task for approval (awaiting parent review)
     */
    COMPLETED,

    /**
     * Task was cancelled before completion (e.g., child unpicked from marketplace)
     */
    CANCELLED
}
