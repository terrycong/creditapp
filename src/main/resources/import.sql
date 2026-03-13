-- 插入家长账号（密码: parent123）
INSERT INTO users (username, password, role, points) VALUES ('parent', '$2a$10$BFeE1qUtBvthU1sKwEc.tOIzuFOw1D635dDscfC2cv1HcP1/Co0Su', 'PARENT', 0);

-- 插入小孩账号（密码: child123）
-- Insert into children table directly since Child is a separate entity
INSERT INTO children (username, password, role, parent_id, points) VALUES ('child', '$2a$10$cV03s.di3hDvqXLPMiAvpucc7aLcLrWv5kHMFWIrOZXWdAtT4SsDi', 'CHILD', 1, 0);

-- 插入示例任务
-- 直接分配给孩子的任务
INSERT INTO tasks (title, description, points, type, status, created_by_id, assigned_child_id, active, created_at) VALUES
('完成作业', '按时完成学校作业', 10, 'DAILY_ONCE', 'APPROVED', 1, 1, true, NOW()),
('打扫房间', '整理自己的房间', 5, 'REPEATABLE', 'APPROVED', 1, 1, true, NOW()),
('阅读书籍', '每天阅读 30 分钟课外书', 8, 'DAILY_ONCE', 'APPROVED', 1, 1, true, NOW()),
('帮助做家务', '帮忙洗碗或扫地', 7, 'REPEATABLE', 'APPROVED', 1, 1, true, NOW());

-- 市场任务（不分配给特定孩子，谁都可以领取）
INSERT INTO tasks (title, description, points, type, status, created_by_id, assigned_child_id, picked_by_child_id, active, created_at) VALUES
('洗碗一次', '帮忙洗晚餐的碗', 15, 'REPEATABLE', 'APPROVED', 1, NULL, NULL, true, NOW()),
('倒垃圾', '把家里的垃圾倒掉', 5, 'DAILY_ONCE', 'APPROVED', 1, NULL, NULL, true, NOW()),
('整理客厅', '整理客厅的沙发和桌子', 10, 'REPEATABLE', 'APPROVED', 1, NULL, NULL, true, NOW());

-- 插入示例礼物
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES
('游戏时间', '30分钟游戏时间', 999, 20, true),
('零花钱', '10元零花钱', 50, 100, true),
('外出游玩', '周末去公园玩', 5, 200, true),
('冰淇淋', '喜欢的冰淇淋一份', 999, 15, true),
('新玩具', '买一个喜欢的玩具', 999, 500, true),
('额外积分', '给额外积分奖励', 999, 50, true);