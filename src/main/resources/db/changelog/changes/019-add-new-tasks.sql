--liquibase formatted sql

--changeset admin:019-add-new-tasks
--comment: Add new task list (24 tasks) for comprehensive daily routine and study incentives

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
-- Insert new tasks (24 items)
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
VALUES ('认真做一张课外练习卷', '认真完成一张课外练习卷', 60, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('每天背单词', '每天背诵英语单词', 20, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('认真读完一本书', '认真完整读完一本书', 200, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('写读后感或者观后感 300 以上', '写读后感或观后感 300 字以上', 200, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('复习错别字（1 页）', '复习错别字 1 页', 30, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('爸爸抽查单词 10 个全对', '爸爸抽查 10 个单词全部正确', 50, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('爸爸抽查单词 10 个对 9 个', '爸爸抽查 10 个单词对 9 个', 5, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

-- Weekend tasks
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('周末作业在星期五做完', '周末作业在星期五完成', 200, 'WEEKLY', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('周末作业在星期六做完', '周末作业在星期六完成', 50, 'WEEKLY', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

-- Exam and course tasks
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('考试 95 分以上', '考试成绩 95 分以上', 100, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('认真学习宝典课两节课（30 分钟以上）', '认真学习宝典课两节课（30 分钟以上）', 60, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('认真学习洋葱课（30 分钟以上）', '认真学习洋葱课（30 分钟以上）', 60, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('和爸爸学习（有空的话）30 分钟以上', '和爸爸一起学习 30 分钟以上', 60, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

-- Evening routine tasks
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('9:30 前冲完凉', '晚上 9:30 前洗完澡', 15, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('10:30 前上床睡觉', '晚上 10:30 前上床睡觉', 10, 'DAILY_ONCE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

-- Rollback comment: This is a one-time data migration. To rollback, restore from backup or re-run previous changeset.
