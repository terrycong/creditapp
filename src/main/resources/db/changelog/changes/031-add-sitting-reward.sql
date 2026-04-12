--liquibase formatted sql

--changeset admin:031-add-sitting-reward
--comment: Add reward for staying seated during meals

-- ============================================
-- Insert new reward: 吃饭不离开座位
-- ============================================

-- Reward: 一天吃饭不离开座位 (5 points)
INSERT INTO rewards (name, description, quantity, points_required, active) 
VALUES ('一天吃饭不离开座位', '吃饭时保持坐姿不离开座位，培养良好用餐习惯', 999, 5, true);

-- Rollback comment: This is a one-time data migration.
