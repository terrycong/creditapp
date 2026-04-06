-- ============================================================
-- creditapp Initial Data (DML)
-- ============================================================
-- 家庭积分管理系统 - 初始数据和配置
-- Generated: 2026-04-06
-- ============================================================

--liquibase formatted sql

-- ============================================================
-- 1. 基础用户数据
-- ============================================================

--changeset admin:data-01-users
--comment: Insert parent and child users
INSERT INTO users (username, password, role, points) 
SELECT 'parent', '$2a$10$BFeE1qUtBvthU1sKwEc.tOIzuFOw1D635dDscfC2cv1HcP1/Co0Su', 'PARENT', 0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'parent');

INSERT INTO children (username, password, role, parent_id, points)
SELECT 'child', '$2a$10$cV03s.di3hDvqXLPMiAvpucc7aLcLrWv5kHMFWIrOZXWdAtT4SsDi', 'CHILD', u.id, 0
FROM users u WHERE u.username = 'parent'
AND NOT EXISTS (SELECT 1 FROM children WHERE username = 'child');

-- ============================================================
-- 2. 日常任务（晨间惯例）
-- ============================================================

--changeset admin:data-02-tasks-morning
--comment: Insert morning routine tasks
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at)
SELECT t.title, t.description, t.points, t.type, 'APPROVED', u.id, true, NOW()
FROM users u, (
    SELECT '不赖床' AS title, '按时起床不赖床' AS description, 5 AS points, 'DAILY_ONCE' AS type UNION ALL
    SELECT '主动吃维生素', '主动吃维生素', 5, 'DAILY_ONCE' UNION ALL
    SELECT '没人叫就刷牙', '主动刷牙不需要提醒', 5, 'DAILY_ONCE' UNION ALL
    SELECT '吃光早餐', '吃完早餐不浪费', 10, 'DAILY_ONCE' UNION ALL
    SELECT '每天喝完两瓶牛奶', '每天喝完两瓶牛奶', 15, 'DAILY_ONCE' UNION ALL
    SELECT '配合打针', '配合打针不哭闹', 20, 'REPEATABLE'
) t
WHERE u.username = 'parent'
AND NOT EXISTS (SELECT 1 FROM tasks WHERE title = t.title);

-- ============================================================
-- 3. 学习任务
-- ============================================================

--changeset admin:data-03-tasks-study
--comment: Insert study-related tasks
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at)
SELECT t.title, t.description, t.points, t.type, 'APPROVED', u.id, true, NOW()
FROM users u, (
    SELECT '上学不迟到' AS title, '按时上学不迟到' AS description, 5 AS points, 'DAILY_ONCE' AS type UNION ALL
    SELECT '（非周末）回家前做完作业', '非周末当天回家前完成作业', 50, 'DAILY_ONCE' UNION ALL
    SELECT '9 点前做完作业', '晚上 9 点前完成作业', 20, 'DAILY_ONCE' UNION ALL
    SELECT '9:30 前冲完凉', '晚上 9:30 前洗完澡', 15, 'DAILY_ONCE' UNION ALL
    SELECT '10:30 前上床睡觉', '晚上 10:30 前上床睡觉', 10, 'DAILY_ONCE' UNION ALL
    SELECT '认真做一张课外练习卷', '认真完成一张课外练习卷', 60, 'REPEATABLE'
) t
WHERE u.username = 'parent'
AND NOT EXISTS (SELECT 1 FROM tasks WHERE title = t.title);

-- ============================================================
-- 4. 学习习惯任务
-- ============================================================

--changeset admin:data-04-tasks-habits
--comment: Insert study habit tasks
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at)
SELECT t.title, t.description, t.points, t.type, 'APPROVED', u.id, true, NOW()
FROM users u, (
    SELECT '坚持背单词 10 天' AS title, '连续坚持背单词 10 天，培养学习习惯' AS description, 50 AS points, 'ONE_TIME' AS type UNION ALL
    SELECT '坚持背单词 20 天', '连续坚持背单词 20 天，持之以恒', 120, 'ONE_TIME' UNION ALL
    SELECT '坚持背单词 50 天', '连续坚持背单词 50 天，坚持不懈', 300, 'ONE_TIME' UNION ALL
    SELECT '一周学习 3 节宝典课', '每周完成 3 节宝典课程学习', 50, 'REPEATABLE'
) t
WHERE u.username = 'parent'
AND NOT EXISTS (SELECT 1 FROM tasks WHERE title = t.title);

-- ============================================================
-- 5. 新增数学任务
-- ============================================================

--changeset admin:data-05-tasks-math
--comment: Insert math exercise task
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at)
SELECT '每天做 30 道数学题', '每天完成 30 道数学练习题，正确率达到 90% 或以上', 10, 'DAILY_ONCE', 'APPROVED', u.id, true, NOW()
FROM users u WHERE u.username = 'parent'
AND NOT EXISTS (SELECT 1 FROM tasks WHERE title = '每天做 30 道数学题');

-- ============================================================
-- 6. 基础奖励
-- ============================================================

--changeset admin:data-06-rewards-basic
--comment: Insert basic rewards
INSERT INTO rewards (name, description, quantity, points_required, active)
SELECT t.name, t.description, 999, t.points, true
FROM (
    SELECT '游戏时间' AS name, '30 分钟游戏时间' AS description, 20 AS points UNION ALL
    SELECT '看电影', '选择一部喜欢的电影观看', 40 UNION ALL
    SELECT '冰淇淋', '喜欢的冰淇淋一份', 15 UNION ALL
    SELECT '零花钱', '10 元零花钱', 100 UNION ALL
    SELECT '新玩具', '买一个喜欢的玩具', 500 UNION ALL
    SELECT '图书', '购买一本喜欢的图书', 40 UNION ALL
    SELECT '文具套装', '获得一套新文具', 50
) t
WHERE NOT EXISTS (SELECT 1 FROM rewards WHERE name = t.name);

-- ============================================================
-- 7. 餐饮奖励
-- ============================================================

--changeset admin:data-07-rewards-food
--comment: Insert food and dining rewards
INSERT INTO rewards (name, description, quantity, points_required, active)
SELECT t.name, t.description, 999, t.points, true
FROM (
    SELECT '去游乐场' AS name, '周末去游乐场玩半天' AS description, 150 AS points UNION ALL
    SELECT '披萨大餐', '全家一起吃披萨', 100 UNION ALL
    SELECT '去指定的餐厅吃饭', '去指定的餐厅吃饭', 50 UNION ALL
    SELECT '零食一份', '零食一份', 60 UNION ALL
    SELECT '饮料 1 支', '饮料 1 支', 60
) t
WHERE NOT EXISTS (SELECT 1 FROM rewards WHERE name = t.name);

-- ============================================================
-- 8. 特权奖励
-- ============================================================

--changeset admin:data-08-rewards-privileges
--comment: Insert privilege rewards
INSERT INTO rewards (name, description, quantity, points_required, active)
SELECT t.name, t.description, 999, t.points, true
FROM (
    SELECT '选择周末活动' AS name, '决定周末全家去哪里玩' AS description, 80 AS points UNION ALL
    SELECT '晚睡 1 小时特权', '周末可以晚睡 1 小时', 40 UNION ALL
    SELECT '免做家务一次', '可以免除一次家务任务', 25 UNION ALL
    SELECT '免除扣分一次', '免除扣分一次', 100 UNION ALL
    SELECT '免除责罚一次', '免除责罚一次', 200
) t
WHERE NOT EXISTS (SELECT 1 FROM rewards WHERE name = t.name);

-- ============================================================
-- 9. 亲子活动奖励
-- ============================================================

--changeset admin:data-09-rewards-family
--comment: Insert family activity rewards
INSERT INTO rewards (name, description, quantity, points_required, active)
SELECT t.name, t.description, 999, t.points, true
FROM (
    SELECT '和爸爸玩游戏半小时' AS name, '和爸爸玩游戏半小时' AS description, 120 AS points UNION ALL
    SELECT '和爸爸一起拼模型 1 小时', '和爸爸一起拼模型 1 小时', 20 UNION ALL
    SELECT '和爸爸一起拼拼图 1 小时', '和爸爸一起拼拼图 1 小时', 30 UNION ALL
    SELECT '和爸爸看一个 10 分钟内的短视频', '和爸爸看一个 10 分钟内的短视频', 10 UNION ALL
    SELECT '下载故事 1 小时', '下载故事 1 小时', 50 UNION ALL
    SELECT '下载一个半小时的故事', '下载一个半小时的故事', 70 UNION ALL
    SELECT '去指定的餐厅吃饭', '去指定的餐厅吃饭', 50
) t
WHERE NOT EXISTS (SELECT 1 FROM rewards WHERE name = t.name);

-- ============================================================
-- 10. 大奖奖励
-- ============================================================

--changeset admin:data-10-rewards-grand
--comment: Insert grand rewards
INSERT INTO rewards (name, description, quantity, points_required, active)
SELECT t.name, t.description, 999, t.points, true
FROM (
    SELECT '住酒店一晚' AS name, '住酒店一晚' AS description, 500 AS points UNION ALL
    SELECT '一盒新模型', '一盒新模型', 300 UNION ALL
    SELECT '看电影一部', '看电影一部', 100
) t
WHERE NOT EXISTS (SELECT 1 FROM rewards WHERE name = t.name);

-- ============================================================
-- 11. 惩罚规则
-- ============================================================

--changeset admin:data-11-penalty-rules
--comment: Insert penalty rules
INSERT INTO penalty_rules (name, description, points, active, created_by_id, created_at)
SELECT t.name, t.description, t.points, true, u.id, NOW()
FROM users u, (
    SELECT '不诚实使用平板' AS name, '不诚实使用平板电脑，包括偷玩游戏、浏览不良内容等' AS description, 50 AS points UNION ALL
    SELECT '衣着不整', '外出时衣着不整洁', 10
) t
WHERE u.username = 'parent'
AND NOT EXISTS (SELECT 1 FROM penalty_rules WHERE name = t.name);

-- ============================================================
-- 12. 抽奖主题
-- ============================================================

--changeset admin:data-12-lottery-theme
--comment: Insert lottery theme
INSERT INTO lottery_themes (name, description, points_per_draw, type, active, created_by_id, created_at)
SELECT '幸运大转盘', '每日抽奖机会，试试你的运气！', 10, 'WEIGHTED_RANDOM', true, u.id, NOW()
FROM users u WHERE u.username = 'parent'
AND NOT EXISTS (SELECT 1 FROM lottery_themes WHERE name = '幸运大转盘');

-- ============================================================
-- 13. 抽奖奖品
-- ============================================================

--changeset admin:data-13-lottery-prizes
--comment: Insert lottery prizes
INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, quantity, redeemed_count, active, reward_id, created_at)
SELECT lt.id, t.name, t.description, t.weight, 999, 0, true, r.id, NOW()
FROM lottery_themes lt, rewards r, (
    SELECT '冰淇淋' AS name, '喜欢的冰淇淋一份' AS description, 50 AS weight UNION ALL
    SELECT '游戏时间', '30 分钟游戏时间', 30 UNION ALL
    SELECT '零花钱', '10 元零花钱', 15 UNION ALL
    SELECT '新玩具', '买一个喜欢的玩具', 5
) t
WHERE lt.name = '幸运大转盘'
AND r.name = t.name
AND NOT EXISTS (SELECT 1 FROM lottery_prizes lp WHERE lp.lottery_theme_id = lt.id AND lp.name = t.name);

-- ============================================================
-- 14. 通知配置（可选）
-- ============================================================

--changeset admin:data-14-notification-prefs
--comment: Insert default notification preferences
INSERT INTO notification_preferences (user_id, task_reminder, reward_approved, penalty_applied, system_announcement)
SELECT id, true, true, true, true FROM users WHERE username = 'parent'
AND NOT EXISTS (SELECT 1 FROM notification_preferences);

-- ============================================================
-- Data initialization complete
-- ============================================================
