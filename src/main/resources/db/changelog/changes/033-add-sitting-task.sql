--liquibase formatted sql

--changeset admin:033-add-sitting-task
--comment: Add task for staying seated during meals

-- ============================================
-- Insert new task: 吃饭不离开座位
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
    '吃饭不离开座位', 
    '吃饭时保持坐姿不离开座位，直到吃完为止，培养良好用餐习惯', 
    5, 
    'DAILY_ONCE', 
    'APPROVED', 
    true, 
    NOW()
);

-- Rollback comment: This is a one-time data migration.
