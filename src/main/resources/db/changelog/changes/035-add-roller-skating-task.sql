--liquibase formatted sql

--changeset admin:035-add-roller-skating-task
--comment: Add task for roller skating with 75 points reward

-- ============================================
-- Insert new task: 认真参与轮滑
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
    '认真参与轮滑',
    '认真参与轮滑运动一次，锻炼身体协调能力和运动技能',
    75,
    'DAILY_ONCE',
    'APPROVED',
    true,
    NOW()
);

-- Rollback comment: This is a one-time data migration.
