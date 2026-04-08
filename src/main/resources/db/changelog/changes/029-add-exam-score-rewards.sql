--liquibase formatted sql

--changeset admin:029-add-exam-score-rewards
--comment: Add exam score rewards (2 items): 90+ points = 50 credits, 95+ points = 200 credits

-- ============================================
-- Insert exam score rewards
-- ============================================

INSERT INTO rewards (name, description, quantity, points_required, active) 
VALUES ('考试 90 分以上奖励', '考试 90 分以上，奖励 50 分', 999, 50, true);

INSERT INTO rewards (name, description, quantity, points_required, active) 
VALUES ('考试 95 分以上奖励', '考试 95 分以上，奖励 200 分', 999, 200, true);

-- Rollback comment: This is a one-time data migration.
