--liquibase formatted sql

--changeset admin:schema-task-jobs-add-timeline-columns
--comment: Add timeline columns to task_jobs table
-- 修复：TaskJob 实体类中有 startedAt, deadline, updatedAt 字段，但数据库表缺少这些列

ALTER TABLE task_jobs 
ADD COLUMN started_at DATETIME NULL COMMENT 'When the child started working on this task' AFTER assigned_at,
ADD COLUMN deadline DATETIME NULL COMMENT 'Task deadline' AFTER started_at,
ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time' AFTER created_at;
