--liquibase formatted sql

--changeset admin:020-add-penalty-rules
--comment: Add penalty rules (7 rules) for behavior management

-- ============================================
-- Insert penalty rules (7 items)
-- ============================================

INSERT INTO penalty_rules (name, description, points, active, created_by_id, created_at) 
VALUES ('浪费牛奶', '浪费牛奶', 5, true, (SELECT id FROM users WHERE username='parent'), NOW());

INSERT INTO penalty_rules (name, description, points, active, created_by_id, created_at) 
VALUES ('浪费食物', '浪费食物', 10, true, (SELECT id FROM users WHERE username='parent'), NOW());

INSERT INTO penalty_rules (name, description, points, active, created_by_id, created_at) 
VALUES ('不讲卫生', '不讲卫生', 10, true, (SELECT id FROM users WHERE username='parent'), NOW());

INSERT INTO penalty_rules (name, description, points, active, created_by_id, created_at) 
VALUES ('不冲厕所', '不冲厕所', 10, true, (SELECT id FROM users WHERE username='parent'), NOW());

INSERT INTO penalty_rules (name, description, points, active, created_by_id, created_at) 
VALUES ('不讲礼貌', '不讲礼貌', 10, true, (SELECT id FROM users WHERE username='parent'), NOW());

INSERT INTO penalty_rules (name, description, points, active, created_by_id, created_at) 
VALUES ('不诚实', '不诚实', 30, true, (SELECT id FROM users WHERE username='parent'), NOW());

INSERT INTO penalty_rules (name, description, points, active, created_by_id, created_at) 
VALUES ('破坏财物', '破坏财物', 50, true, (SELECT id FROM users WHERE username='parent'), NOW());

-- Rollback comment: This is a one-time data migration.
