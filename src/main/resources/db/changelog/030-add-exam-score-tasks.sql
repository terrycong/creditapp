--liquibase formatted sql

--changeset admin:030-add-exam-score-tasks
--comment: Add exam score tasks: 90+ points = 50 credits, 95+ points = 200 credits

-- ============================================
-- Insert exam score tasks (REPEATABLE - can be earned multiple times)
-- ============================================

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at)
SELECT t.title, t.description, t.points, t.type, 'APPROVED', u.id, true, NOW()
FROM users u, (
    SELECT '考试 90 分以上' AS title, '考试 90 分以上，奖励 50 积分' AS description, 50 AS points, 'REPEATABLE' AS type UNION ALL
    SELECT '考试 95 分以上', '考试 95 分以上，奖励 200 积分', 200, 'REPEATABLE'
) t
WHERE u.username = 'parent';

-- Rollback comment: This is a one-time data migration.
