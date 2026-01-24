-- 插入家长账号（密码: parent123）
INSERT INTO users (username, password, role) VALUES ('parent', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'PARENT');

-- 插入小孩账号（密码: child123）
INSERT INTO users (username, password, role) VALUES ('child', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'CHILD');
INSERT INTO children (id, parent_id, points) VALUES (LAST_INSERT_ID(), 1, 0);

-- 插入示例任务
INSERT INTO tasks (title, description, points, type, status, created_by_id, assigned_child_id, active) VALUES
('完成作业', '按时完成学校作业', 10, 'DAILY_ONCE', 'APPROVED', 1, 2, true),
('打扫房间', '整理自己的房间', 5, 'REPEATABLE', 'APPROVED', 1, 2, true),
('阅读书籍', '每天阅读30分钟课外书', 8, 'DAILY_ONCE', 'APPROVED', 1, 2, true),
('帮助做家务', '帮忙洗碗或扫地', 7, 'REPEATABLE', 'APPROVED', 1, 2, true);

-- 插入示例礼物
INSERT INTO rewards (name, description, quantity, points_required, active) VALUES
('游戏时间', '30分钟游戏时间', 999, 20, true),
('零花钱', '10元零花钱', 50, 100, true),
('外出游玩', '周末去公园玩', 5, 200, true),
('冰淇淋', '喜欢的冰淇淋一份', 999, 15, true),
('新玩具', '买一个喜欢的玩具', 999, 500, true),
('额外积分', '给额外积分奖励', 999, 50, true);