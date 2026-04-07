--liquibase formatted sql

--changeset admin:028-add-documentary-tasks-and-cartoon-rewards
--comment: Add new tasks for documentary watching and new rewards for cartoons and penalty exemption

-- ============================================
-- Insert new tasks: 看纪录片相关任务
-- ============================================

-- Task 1: 课余时间看半小时纪录片 (15 points, daily once)
INSERT INTO tasks (
    title, 
    description, 
    points, 
    type, 
    status, 
    active, 
    created_at
) VALUES (
    '课余时间看半小时纪录片', 
    '课余时间观看半小时纪录片，增长知识', 
    15, 
    'DAILY_ONCE', 
    'APPROVED', 
    true, 
    NOW()
);

-- Task 2: 看纪录片后做笔记 100 字以上 (15 points, daily once)
INSERT INTO tasks (
    title, 
    description, 
    points, 
    type, 
    status, 
    active, 
    created_at
) VALUES (
    '看纪录片后做笔记', 
    '观看纪录片后写观后感或笔记，字数 100 字以上', 
    15, 
    'DAILY_ONCE', 
    'APPROVED', 
    true, 
    NOW()
);

-- ============================================
-- Insert new rewards: 动画片和免罚站券
-- ============================================

-- Reward 1: 看动画片半小时 (20 points)
INSERT INTO rewards (name, description, quantity, points_required, active) 
VALUES ('看动画片半小时', '观看动画片半小时', 999, 20, true);

-- Reward 2: 免罚站券半小时 (100 points)
INSERT INTO rewards (name, description, quantity, points_required, active) 
VALUES ('免罚站券半小时', '免除半小时罚站惩罚', 999, 100, true);

-- Rollback comment: This is a one-time data migration.
