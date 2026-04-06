--liquibase formatted sql

--changeset admin:026-add-story-download-reward
--comment: Add new reward: download story for 1.5 hours (70 points)

-- ============================================
-- Insert new reward: 下载一个半小时的故事
-- ============================================

INSERT INTO rewards (name, description, quantity, points_required, active) 
VALUES ('下载一个半小时的故事', '下载一个半小时的故事', 999, 70, true);

-- Rollback comment: This is a one-time data migration.
