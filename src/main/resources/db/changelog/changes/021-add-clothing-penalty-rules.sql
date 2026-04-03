--liquibase formatted sql

--changeset admin:021-add-clothing-penalty-rules
--comment: Add clothing-related penalty rules (2 rules)

-- ============================================
-- Insert penalty rules (2 items)
-- ============================================

INSERT INTO penalty_rules (name, description, points, active, created_by_id, created_at) 
VALUES ('不穿裤子', '不穿裤子', 5, true, (SELECT id FROM users WHERE username='parent'), NOW());

INSERT INTO penalty_rules (name, description, points, active, created_by_id, created_at) 
VALUES ('不穿拖鞋提醒后不立刻改正', '不穿拖鞋提醒后不立刻改正', 5, true, (SELECT id FROM users WHERE username='parent'), NOW());

-- Rollback comment: This is a one-time data migration.
