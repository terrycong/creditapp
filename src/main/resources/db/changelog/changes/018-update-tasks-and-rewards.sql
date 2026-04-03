--liquibase formatted sql

--changeset admin:018-update-tasks-and-rewards
--comment: Update tasks (3 items) and rewards (15 items) for new point system

-- ============================================
-- Update Tasks (3 items)
-- ============================================

-- Update existing task: 完成作业 (10 points)
UPDATE tasks 
SET points = 10, 
    description = '按时完成学校作业',
    type = 'DAILY_ONCE',
    updated_at = NOW()
WHERE title = '完成作业';

-- Update existing task: 阅读书籍 (20 points)
UPDATE tasks 
SET points = 20, 
    description = '每天阅读 30 分钟课外书',
    type = 'DAILY_ONCE',
    updated_at = NOW()
WHERE title = '阅读书籍';

-- Update existing task: 帮助做家务 (5 points)
UPDATE tasks 
SET points = 5, 
    description = '帮忙洗碗或扫地',
    type = 'REPEATABLE',
    updated_at = NOW()
WHERE title = '帮助做家务';

-- Remove old task-related data in correct order (due to foreign keys)
-- First delete from task_completions (child of task_jobs)
DELETE FROM task_completions WHERE task_job_id IN (
    SELECT id FROM task_jobs WHERE task_id IN (
        SELECT id FROM tasks WHERE title NOT IN ('完成作业', '阅读书籍', '帮助做家务')
    )
);

-- Then delete from task_jobs (child of tasks)
DELETE FROM task_jobs WHERE task_id IN (
    SELECT id FROM tasks WHERE title NOT IN ('完成作业', '阅读书籍', '帮助做家务')
);

-- Finally delete old tasks
DELETE FROM tasks WHERE title NOT IN ('完成作业', '阅读书籍', '帮助做家务');

-- ============================================
-- Update Rewards (15 items)
-- ============================================

-- Clear existing rewards
DELETE FROM lottery_prizes;
DELETE FROM lottery_themes;
DELETE FROM reward_redemptions;
DELETE FROM rewards;

-- Insert new rewards
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES 
('10 分钟上网券', '上网 10 分钟', 999, 50, true),
('20 分钟上网券', '上网 20 分钟', 999, 100, true),
('30 分钟上网券', '上网 30 分钟', 999, 150, true),
('1 小时上网券', '上网 1 小时', 999, 200, true),
('雪糕一个', '雪糕一个', 999, 60, true),
('零食一份', '零食一份', 999, 60, true),
('饮料 1 支', '饮料 1 支', 999, 60, true),
('挖洞洞 1 次', '挖洞洞 1 次', 999, 120, true),
('住酒店一晚', '住酒店一晚', 999, 500, true),
('看电影一部', '看电影一部', 999, 100, true),
('和爸爸玩游戏半小时', '和爸爸玩游戏半小时', 999, 120, true),
('去指定的餐厅吃饭', '去指定的餐厅吃饭', 999, 50, true),
('下载故事 1 小时', '下载故事 1 小时', 999, 50, true),
('免除责罚一次', '免除责罚一次', 999, 200, true),
('免除扣分一次', '免除扣分一次', 999, 100, true);

-- ============================================
-- Re-create Lottery Theme and Prizes
-- ============================================

-- Insert lottery theme
INSERT INTO lottery_themes (name, description, points_per_draw, type, active, created_by_id, created_at) 
VALUES ('幸运大转盘', '每日抽奖机会，试试你的运气！', 10, 'WEIGHTED_RANDOM', true, 
        (SELECT id FROM users WHERE username='parent'), NOW());

-- Insert lottery prizes with weights
INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, quantity, redeemed_count, active, reward_id, created_at)
SELECT lt.id, '10 分钟上网券', '上网 10 分钟', 30, 999, 0, true, r.id, NOW()
FROM lottery_themes lt, rewards r
WHERE lt.name = '幸运大转盘' AND r.name = '10 分钟上网券';

INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, quantity, redeemed_count, active, reward_id, created_at)
SELECT lt.id, '雪糕一个', '雪糕一个', 25, 999, 0, true, r.id, NOW()
FROM lottery_themes lt, rewards r
WHERE lt.name = '幸运大转盘' AND r.name = '雪糕一个';

INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, quantity, redeemed_count, active, reward_id, created_at)
SELECT lt.id, '看电影一部', '看电影一部', 20, 999, 0, true, r.id, NOW()
FROM lottery_themes lt, rewards r
WHERE lt.name = '幸运大转盘' AND r.name = '看电影一部';

INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, quantity, redeemed_count, active, reward_id, created_at)
SELECT lt.id, '住酒店一晚', '住酒店一晚', 5, 999, 0, true, r.id, NOW()
FROM lottery_themes lt, rewards r
WHERE lt.name = '幸运大转盘' AND r.name = '住酒店一晚';

-- Rollback comment: This is a one-time data migration. To rollback, restore from backup or re-run 004-clear-and-reseed.yaml
