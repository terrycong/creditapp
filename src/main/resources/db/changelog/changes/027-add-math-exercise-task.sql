--liquibase formatted sql

--changeset admin:027-add-math-exercise-task
--comment: Add new daily task: Complete 30 math exercises with 90% accuracy for 10 points

-- ============================================
-- Insert new task: 每天做 30 道数学题，正确率 90% 以上
-- ============================================

INSERT INTO tasks (
    title, 
    description, 
    points, 
    type, 
    status, 
    active, 
    created_at
) VALUES (
    '每天做 30 道数学题', 
    '每天完成 30 道数学练习题，正确率达到 90% 或以上', 
    10, 
    'DAILY_ONCE', 
    'APPROVED', 
    true, 
    NOW()
);

-- Rollback comment: This is a one-time data migration.
