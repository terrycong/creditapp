-- Liquibase ChangeSet: 006-update-tasks-and-rewards
-- Description: Update tasks and rewards to new configuration (2026-04-03)
-- This will DELETE existing data and reseed with new values

-- Clear existing tasks and rewards
DELETE FROM lottery_prizes;
DELETE FROM lottery_themes;
DELETE FROM rewards;
DELETE FROM tasks;

-- Insert new tasks (all in marketplace, 3 items)
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) VALUES 
('完成作业', '按时完成学校作业', 10, 'DAILY_ONCE', 'APPROVED', 1, true, NOW()),
('阅读书籍', '每天阅读 30 分钟课外书', 20, 'DAILY_ONCE', 'APPROVED', 1, true, NOW()),
('帮助做家务', '帮忙洗碗或扫地', 5, 'REPEATABLE', 'APPROVED', 1, true, NOW());

-- Insert new rewards (15 items)
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

-- Insert lottery theme
INSERT INTO lottery_themes (name, description, points_per_draw, type, active, created_by_id, created_at) VALUES 
('幸运大转盘', '每日抽奖机会，试试你的运气！', 10, 'WEIGHTED_RANDOM', true, 1, NOW());

-- Insert lottery prizes (linked to reward IDs 1, 5, 10, 9)
INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, probability, quantity, redeemed_count, active, reward_id, created_at) VALUES 
(1, '10 分钟上网券', '上网 10 分钟', 30, NULL, 999, 0, true, 1, NOW()),
(1, '雪糕一个', '雪糕一个', 25, NULL, 999, 0, true, 5, NOW()),
(1, '看电影一部', '看电影一部', 20, NULL, 999, 0, true, 10, NOW()),
(1, '住酒店一晚', '住酒店一晚', 5, NULL, 999, 0, true, 9, NOW());
