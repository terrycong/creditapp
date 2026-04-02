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

-- Rewards
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('游戏时间', '30 分钟游戏时间', 999, 20, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('看电影', '选择一部喜欢的电影观看', 999, 40, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('去游乐场', '周末去游乐场玩半天', 999, 150, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('冰淇淋', '喜欢的冰淇淋一份', 999, 15, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('零花钱', '10 元零花钱', 50, 100, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('披萨大餐', '全家一起吃披萨', 999, 100, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('新玩具', '买一个喜欢的玩具', 999, 500, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('图书', '购买一本喜欢的图书', 999, 40, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('文具套装', '获得一套新文具', 999, 50, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('选择周末活动', '决定周末全家去哪里玩', 999, 80, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('晚睡 1 小时特权', '周末可以晚睡 1 小时', 999, 40, true);
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES ('免做家务一次', '可以免除一次家务任务', 999, 25, true);

-- Lottery Theme
INSERT INTO lottery_themes (name, description, points_per_draw, type, active, created_by_id, created_at) VALUES ('幸运大转盘', '每日抽奖机会，试试你的运气！', 10, 'WEIGHTED_RANDOM', true, 1, NOW());

-- Lottery Prizes
INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, probability, quantity, redeemed_count, active, reward_id, created_at) VALUES (1, '冰淇淋', '喜欢的冰淇淋一份', 50, NULL, 999, 0, true, 4, NOW());
INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, probability, quantity, redeemed_count, active, reward_id, created_at) VALUES (1, '游戏时间', '30 分钟游戏时间', 30, NULL, 999, 0, true, 1, NOW());
INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, probability, quantity, redeemed_count, active, reward_id, created_at) VALUES (1, '零花钱', '10 元零花钱', 15, NULL, 50, 0, true, 5, NOW());
INSERT INTO lottery_prizes (lottery_theme_id, name, description, weight, probability, quantity, redeemed_count, active, reward_id, created_at) VALUES (1, '新玩具', '买一个喜欢的玩具', 5, NULL, 999, 0, true, 7, NOW());
