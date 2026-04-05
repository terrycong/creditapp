--liquibase formatted sql

--changeset admin:024-add-word-study-rewards
--comment: Add word study streak rewards and penalty rules

-- ============================================
-- 坚持背单词奖励任务
-- ============================================

-- 坚持背单词 10 天，奖励 50 分
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('坚持背单词 10 天', '连续坚持背单词 10 天，培养学习习惯', 50, 'ONE_TIME', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

-- 坚持背单词 20 天，奖励 120 分
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('坚持背单词 20 天', '连续坚持背单词 20 天，持之以恒', 120, 'ONE_TIME', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

-- 坚持背单词 50 天，奖励 300 分
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('坚持背单词 50 天', '连续坚持背单词 50 天，坚持不懈', 300, 'ONE_TIME', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

-- ============================================
-- 宝典课学习任务
-- ============================================

-- 一周学习 3 节宝典课，奖励 50 分
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) 
VALUES ('一周学习 3 节宝典课', '每周完成 3 节宝典课程学习', 50, 'REPEATABLE', 'APPROVED', (SELECT id FROM users WHERE username='parent'), true, NOW());

-- ============================================
-- 违规扣分规则（使用 penalty_rules 表）
-- ============================================

-- 不诚实使用平板，扣 50 分
INSERT INTO penalty_rules (name, description, points, active, created_by_id, created_at) 
VALUES ('不诚实使用平板', '不诚实使用平板电脑，包括偷玩游戏、浏览不良内容等', 50, true, (SELECT id FROM users WHERE username='parent'), NOW());

-- Rollback comment: This is a one-time data migration.
