--liquibase formatted sql

--changeset admin:034-add-computer-learning-task
--comment: Add task for learning computer with dad

-- ============================================
-- Insert new task: 跟爸爸一起学电脑
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
    '跟爸爸一起学电脑', 
    '跟爸爸一起学习电脑知识半小时，培养计算机技能', 
    25, 
    'DAILY_ONCE', 
    'APPROVED', 
    true, 
    NOW()
);

-- Rollback comment: This is a one-time data migration.
