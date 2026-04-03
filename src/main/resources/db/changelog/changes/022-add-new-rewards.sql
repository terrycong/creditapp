--liquibase formatted sql

--changeset admin:022-add-new-rewards
--comment: Add new rewards (3 items): model kit, puzzle time with dad

-- ============================================
-- Insert new rewards (3 items)
-- ============================================

INSERT INTO rewards (name, description, quantity, points_required, active) 
VALUES ('一盒新模型', '一盒新模型', 999, 300, true);

INSERT INTO rewards (name, description, quantity, points_required, active) 
VALUES ('和爸爸一起拼模型 1 小时', '和爸爸一起拼模型 1 小时', 999, 20, true);

INSERT INTO rewards (name, description, quantity, points_required, active) 
VALUES ('和爸爸一起拼拼图 1 小时', '和爸爸一起拼拼图 1 小时', 999, 30, true);

-- Rollback comment: This is a one-time data migration.
