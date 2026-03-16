-- ============================================================================
-- 抽奖系统 - 初始化数据
-- Lottery System - Initial Data Seed
-- ============================================================================
-- 创建时间: 2026-03-16
-- 说明: 提供多种抽奖方案，包含不同档次的奖品池
-- ============================================================================

-- ============================================================================
-- 一、抽奖主题 (lottery_themes)
-- ============================================================================

-- 方案1: 基础抽奖 - 适合日常娱乐
INSERT INTO lottery_themes (name, description, points_per_draw, type, active, created_by_id, created_at, updated_at) VALUES
('基础抽奖', '每次消耗10积分，有机会获得巧克力、糖果等小奖品', 10, 'FIXED_PROBABILITY', true, 1, NOW(), NOW());

-- 方案2: 幸运抽奖 - 中等消耗，奖品更好
INSERT INTO lottery_themes (name, description, points_per_draw, type, active, created_by_id, created_at, updated_at) VALUES
('幸运抽奖', '每次消耗30积分，奖品更丰厚', 30, 'FIXED_PROBABILITY', true, 1, NOW(), NOW());

-- 方案3: 超级抽奖 - 高消耗，大奖机会
INSERT INTO lottery_themes (name, description, points_per_draw, type, active, created_by_id, created_at, updated_at) VALUES
('超级抽奖', '每次消耗100积分，有机会赢取超值大奖', 100, 'WEIGHTED_RANDOM', true, 1, NOW(), NOW());

-- 方案4: 必中抽奖 - 稳定获得奖励
INSERT INTO lottery_themes (name, description, points_per_draw, type, active, created_by_id, created_at, updated_at) VALUES
('必中抽奖', '每次消耗50积分，100%中奖，随机获得奖励', 50, 'GUARANTEED', true, 1, NOW(), NOW());

-- ============================================================================
-- 二、抽奖奖品 (lottery_prizes)
-- ============================================================================

-- ========== 基础抽奖奖品 (theme_id = 1) ==========
-- 总概率: 10% + 30% + 10% + 50% = 100%

-- 巧克力 - 10%概率
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(1, '巧克力', '美味的巧克力一块', 15, 1, 10, 100, 0, true, NOW(), NOW());

-- 糖果 - 30%概率
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(1, '糖果', '甜蜜的糖果一包', 8, 1, 30, 200, 0, true, NOW(), NOW());

-- 小贴纸 - 10%概率
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(1, '小贴纸', '可爱的卡通贴纸一张', 5, 1, 10, 500, 0, true, NOW(), NOW());

-- 谢谢参与 - 50%概率（不中奖）
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(1, '谢谢参与', '再接再厉，下次一定中奖！', 0, 1, 50, -1, 0, true, NOW(), NOW());


-- ========== 幸运抽奖奖品 (theme_id = 2) ==========
-- 总概率: 5% + 15% + 20% + 25% + 35% = 100%

-- 冰淇淋券 - 5%概率
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(2, '冰淇淋券', '可以去兑换冰淇淋一份', 50, 1, 5, 50, 0, true, NOW(), NOW());

-- 零食大礼包 - 15%概率
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(2, '零食大礼包', '各种美味零食组合', 40, 1, 15, 80, 0, true, NOW(), NOW());

-- 文具套装 - 20%概率
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(2, '文具套装', '铅笔、橡皮、尺子组合', 30, 1, 20, 100, 0, true, NOW(), NOW());

-- 游戏时间30分钟 - 25%概率
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(2, '游戏时间30分钟', '获得额外30分钟游戏时间', 25, 1, 25, 200, 0, true, NOW(), NOW());

-- 小积分奖励 - 35%概率
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(2, '小积分奖励', '获得10积分奖励', 10, 1, 35, -1, 0, true, NOW(), NOW());


-- ========== 超级抽奖奖品 (theme_id = 3) - 使用权重模式 ==========
-- 权重越高概率越大

-- 特等奖：迪士尼乐园门票 - 权重1（约0.5%）
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(3, '迪士尼乐园门票', '上海迪士尼乐园一日游门票一张', 800, 1, NULL, 5, 0, true, NOW(), NOW());

-- 一等奖：乐高积木大套装 - 权重3（约1.5%）
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(3, '乐高积木大套装', '大型乐高积木一套', 500, 3, NULL, 10, 0, true, NOW(), NOW());

-- 二等奖：新玩具任选 - 权重8（约4%）
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(3, '新玩具任选', '可以选择一个喜欢的玩具（200元以内）', 300, 8, NULL, 20, 0, true, NOW(), NOW());

-- 三等奖：游乐园门票 - 权重15（约7.5%）
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(3, '游乐园门票', '周末去游乐园玩', 200, 15, NULL, 30, 0, true, NOW(), NOW());

-- 四等奖：麦当劳大餐 - 权重25（约12.5%）
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(3, '麦当劳大餐', '去麦当劳吃一顿大餐', 100, 25, NULL, 50, 0, true, NOW(), NOW());

-- 五等奖：积分返还 - 权重50（约25%）
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(3, '积分返还', '返还50积分', 50, 50, NULL, -1, 0, true, NOW(), NOW());

-- 参与奖：小积分 - 权重98（约49%）
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(3, '参与奖', '获得20积分安慰奖', 20, 98, NULL, -1, 0, true, NOW(), NOW());


-- ========== 必中抽奖奖品 (theme_id = 4) - 100%中奖 ==========
-- 使用权重模式，总权重100

-- 大奖：周末出游 - 权重5
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(4, '周末出游', '选择去游乐园或动物园', 200, 5, NULL, 30, 0, true, NOW(), NOW());

-- 中奖：电影票 - 权重10
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(4, '电影票', '去看一场喜欢的电影', 100, 10, NULL, 50, 0, true, NOW(), NOW());

-- 小奖：冰淇淋 - 权重25
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(4, '冰淇淋', '选择喜欢的冰淇淋一份', 50, 25, NULL, 100, 0, true, NOW(), NOW());

-- 普奖：零食 - 权重30
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(4, '零食包', '选择喜欢的零食一份', 30, 30, NULL, 200, 0, true, NOW(), NOW());

-- 安慰奖：积分返还 - 权重30
INSERT INTO lottery_prizes (lottery_theme_id, name, description, `value`, weight, probability, quantity, redeemed_count, active, created_at, updated_at) VALUES
(4, '积分返还', '返还30积分', 30, 30, NULL, -1, 0, true, NOW(), NOW());


-- ============================================================================
-- 三、数据验证查询
-- ============================================================================

-- 查看抽奖主题
SELECT '=== 抽奖主题 ===' AS info;
SELECT id, name, points_per_draw AS 消耗积分, type AS 类型, active AS 启用状态 FROM lottery_themes;

-- 查看各主题的奖品统计
SELECT '=== 各主题奖品统计 ===' AS info;
SELECT 
    t.name AS 主题名称,
    COUNT(p.id) AS 奖品数量,
    SUM(CASE WHEN p.quantity = -1 THEN '无限' ELSE CAST(p.quantity AS CHAR) END) AS 总库存
FROM lottery_themes t
LEFT JOIN lottery_prizes p ON t.id = p.lottery_theme_id
GROUP BY t.id, t.name;

-- 查看基础抽奖的奖品详情
SELECT '=== 基础抽奖奖品详情 ===' AS info;
SELECT 
    name AS 奖品名称,
    description AS 描述,
    `value` AS 价值积分,
    probability AS 概率百分比,
    quantity AS 库存
FROM lottery_prizes 
WHERE lottery_theme_id = 1
ORDER BY probability DESC;

-- 查看总览
SELECT '=== 总览 ===' AS info;
SELECT 
    (SELECT COUNT(*) FROM lottery_themes) AS 抽奖主题数,
    (SELECT COUNT(*) FROM lottery_prizes) AS 奖品总数,
    (SELECT SUM(CASE WHEN quantity > 0 THEN quantity ELSE 0 END) FROM lottery_prizes) AS 有限库存奖品总数;