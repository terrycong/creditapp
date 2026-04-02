-- Liquibase Data Seeding
-- ChangeSet: 002-initial-data

-- Parent User
INSERT INTO users (username, password, role, points) VALUES ('parent', '$2a$10$BFeE1qUtBvthU1sKwEc.tOIzuFOw1D635dDscfC2cv1HcP1/Co0Su', 'PARENT', 0);

-- Child User
INSERT INTO children (username, password, role, parent_id, points) VALUES ('child', '$2a$10$cV03s.di3hDvqXLPMiAvpucc7aLcLrWv5kHMFWIrOZXWdAtT4SsDi', 'CHILD', 1, 0);

-- Direct Tasks
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) VALUES ('完成作业', '按时完成学校作业', 10, 'DAILY_ONCE', 'APPROVED', 1, true, NOW());
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) VALUES ('打扫房间', '整理自己的房间', 5, 'REPEATABLE', 'APPROVED', 1, true, NOW());
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) VALUES ('阅读书籍', '每天阅读 30 分钟课外书', 8, 'DAILY_ONCE', 'APPROVED', 1, true, NOW());
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) VALUES ('帮助做家务', '帮忙洗碗或扫地', 7, 'REPEATABLE', 'APPROVED', 1, true, NOW());

-- Task Jobs
INSERT INTO task_jobs (task_id, child_id, status, snapshot_title, snapshot_description, snapshot_points, snapshot_task_type, assigned_at, created_at) VALUES (1, 1, 'ASSIGNED', '完成作业', '按时完成学校作业', 10, 'DAILY_ONCE', NOW(), NOW());
INSERT INTO task_jobs (task_id, child_id, status, snapshot_title, snapshot_description, snapshot_points, snapshot_task_type, assigned_at, created_at) VALUES (2, 1, 'ASSIGNED', '打扫房间', '整理自己的房间', 5, 'REPEATABLE', NOW(), NOW());
INSERT INTO task_jobs (task_id, child_id, status, snapshot_title, snapshot_description, snapshot_points, snapshot_task_type, assigned_at, created_at) VALUES (3, 1, 'ASSIGNED', '阅读书籍', '每天阅读 30 分钟课外书', 8, 'DAILY_ONCE', NOW(), NOW());
INSERT INTO task_jobs (task_id, child_id, status, snapshot_title, snapshot_description, snapshot_points, snapshot_task_type, assigned_at, created_at) VALUES (4, 1, 'ASSIGNED', '帮助做家务', '帮忙洗碗或扫地', 7, 'REPEATABLE', NOW(), NOW());

-- Marketplace Tasks
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) VALUES ('洗碗一次', '帮忙洗晚餐的碗', 15, 'REPEATABLE', 'APPROVED', 1, true, NOW());
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) VALUES ('倒垃圾', '把家里的垃圾倒掉', 5, 'DAILY_ONCE', 'APPROVED', 1, true, NOW());
INSERT INTO tasks (title, description, points, type, status, created_by_id, active, created_at) VALUES ('整理客厅', '整理客厅的沙发和桌子', 10, 'REPEATABLE', 'APPROVED', 1, true, NOW());

-- Rewards (Updated 2026-04-03)
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('10 分钟上网券', '上网 10 分钟', 999, 50, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('20 分钟上网券', '上网 20 分钟', 999, 100, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('30 分钟上网券', '上网 30 分钟', 999, 150, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('1 小时上网券', '上网 1 小时', 999, 200, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('雪糕一个', '雪糕一个', 999, 60, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('零食一份', '零食一份', 999, 60, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('饮料 1 支', '饮料 1 支', 999, 60, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('挖洞洞 1 次', '挖洞洞 1 次', 999, 120, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('住酒店一晚', '住酒店一晚', 999, 500, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('看电影一部', '看电影一部', 999, 100, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('和爸爸玩游戏半小时', '和爸爸玩游戏半小时', 999, 120, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('去指定的餐厅吃饭', '去指定的餐厅吃饭', 999, 50, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('下载故事 1 小时', '下载故事 1 小时', 999, 50, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('免除责罚一次', '免除责罚一次', 999, 200, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('免除扣分一次', '免除扣分一次', 999, 100, true);

-- Lottery Theme
INSERT INTO lottery_themes (name, description, points_per_draw, type, active, created_by_id, created_at) VALUES ('幸运大转盘', '每日抽奖机会，试试你的运气！', 10, 'WEIGHTED_RANDOM', true, 1, NOW());

-- Lottery Prizes (Updated 2026-04-03)
INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, probability, quantity, redeemed_count, active, reward_id, created_at) VALUES (1, '10 分钟上网券', '上网 10 分钟', 30, NULL, 999, 0, true, 1, NOW());
INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, probability, quantity, redeemed_count, active, reward_id, created_at) VALUES (1, '雪糕一个', '雪糕一个', 25, NULL, 999, 0, true, 5, NOW());
INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, probability, quantity, redeemed_count, active, reward_id, created_at) VALUES (1, '看电影一部', '看电影一部', 20, NULL, 999, 0, true, 10, NOW());
INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, probability, quantity, redeemed_count, active, reward_id, created_at) VALUES (1, '住酒店一晚', '住酒店一晚', 5, NULL, 999, 0, true, 9, NOW());
