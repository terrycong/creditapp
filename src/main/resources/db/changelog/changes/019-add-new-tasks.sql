--liquibase formatted sql

--changeset admin:019-add-new-tasks
--comment: Add new task list (12 tasks) for daily routine and study

-- ============================================
-- Clear existing tasks and related data
-- ============================================

-- First delete from task_completions (child of task_jobs)
DELETE FROM task_completions;

-- Then delete from task_jobs (child of tasks)
DELETE FROM task_jobs;

-- Finally delete all tasks
DELETE FROM tasks;

-- ============================================
-- Insert new tasks (12 items)
-- ============================================

-- Morning routine tasks
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('不赖床', '按时起床不赖床', 5, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('主动吃维生素', '主动吃维生素', 5, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('没人叫就刷牙', '主动刷牙不需要提醒', 5, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('吃光早餐', '吃完早餐不浪费', 10, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('每天喝完两瓶牛奶', '每天喝完两瓶牛奶', 15, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('配合打针', '配合打针不哭闹', 20, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

-- School and study tasks
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('上学不迟到', '按时上学不迟到', 5, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('（非周末）回家前做完作业', '非周末当天回家前完成作业', 50, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('9 点前做完作业', '晚上 9 点前完成作业', 20, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('9:30 前冲完凉', '晚上 9:30 前洗完澡', 15, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('10:30 前上床睡觉', '晚上 10:30 前上床睡觉', 10, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('认真做一张课外练习卷', '认真完成一张课外练习卷', 60, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

-- Rollback comment: This is a one-time data migration.
