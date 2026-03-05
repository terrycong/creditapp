-- ============================================================================
-- 信用积分系统 - 示例数据种子文件
-- Credit Points System - Sample Data Seed
-- ============================================================================
-- 用途：提供丰富的任务和奖励示例数据
-- 使用方式：在 application.properties 中配置 spring.sql.init.mode=embedded 后自动执行
-- 或手动执行：source src/main/resources/data-seed.sql
-- ============================================================================

-- ============================================================================
-- 一、通用鼓励任务 - 学习成长类 (8 个任务)
-- ============================================================================
INSERT INTO tasks (title, description, points, type, status, created_by_id, active) VALUES
('完成数学练习', '独立完成数学练习题 10 道', 12, 'DAILY_ONCE', 'APPROVED', 1, true),
('背诵古诗', '背诵一首新的古诗并默写', 10, 'DAILY_ONCE', 'APPROVED', 1, true),
('英语单词记忆', '学习并记忆 5 个新英语单词', 8, 'DAILY_ONCE', 'APPROVED', 1, true),
('完成科学实验', '完成一个小科学实验并记录结果', 15, 'ONE_TIME', 'APPROVED', 1, true),
('写日记', '记录今天发生的事情和感受', 6, 'DAILY_ONCE', 'APPROVED', 1, true),
('预习新课', '预习明天要学的新课程内容', 10, 'DAILY_ONCE', 'APPROVED', 1, true),
('复习错题', '整理并复习今天的错题', 12, 'DAILY_ONCE', 'APPROVED', 1, true),
('阅读打卡', '阅读课外书并做读书笔记', 10, 'DAILY_ONCE', 'APPROVED', 1, true);

-- ============================================================================
-- 二、生活习惯培养任务 - 日常好习惯 (18 个任务)
-- ============================================================================
-- 早晨习惯
INSERT INTO tasks (title, description, points, type, status, created_by_id, active) VALUES
('早起整理床铺', '起床后整理好自己的床铺', 5, 'DAILY_ONCE', 'APPROVED', 1, true),
('按时起床', '在闹钟响后 5 分钟内起床', 5, 'DAILY_ONCE', 'APPROVED', 1, true),
('晨间洗漱', '独立完成刷牙洗脸', 3, 'DAILY_ONCE', 'APPROVED', 1, true),
('吃早餐', '认真吃完营养早餐', 5, 'DAILY_ONCE', 'APPROVED', 1, true),

-- 卫生习惯
('饭前洗手', '吃饭前主动洗手', 3, 'REPEATABLE', 'APPROVED', 1, true),
('饭后漱口', '饭后漱口保护牙齿', 3, 'REPEATABLE', 'APPROVED', 1, true),
('早晚刷牙', '每天早晚各刷牙一次', 5, 'DAILY_ONCE', 'APPROVED', 1, true),
('洗澡更衣', '勤洗澡勤换衣服', 8, 'REPEATABLE', 'APPROVED', 1, true),
('修剪指甲', '定期修剪指甲保持清洁', 5, 'REPEATABLE', 'APPROVED', 1, true),

-- 整理习惯
('收拾玩具', '玩完玩具后主动收拾整理', 6, 'REPEATABLE', 'APPROVED', 1, true),
('整理书包', '睡前整理好第二天的书包', 6, 'DAILY_ONCE', 'APPROVED', 1, true),
('衣物归位', '脱下的衣服挂好或放入脏衣篮', 4, 'REPEATABLE', 'APPROVED', 1, true),
('保持桌面整洁', '学习后整理书桌', 5, 'DAILY_ONCE', 'APPROVED', 1, true),

-- 作息习惯
('按时睡觉', '晚上 9 点前上床睡觉', 8, 'DAILY_ONCE', 'APPROVED', 1, true),
('午休习惯', '中午适当休息 30 分钟', 5, 'DAILY_ONCE', 'APPROVED', 1, true),

-- 健康习惯
('户外运动', '每天户外活动至少 1 小时', 10, 'DAILY_ONCE', 'APPROVED', 1, true),
('少吃零食', '一天只吃 1 次零食', 8, 'DAILY_ONCE', 'APPROVED', 1, true),
('多喝水', '每天喝够 8 杯水', 6, 'DAILY_ONCE', 'APPROVED', 1, true),
('保护视力', '看电视/手机 30 分钟休息眼睛', 6, 'REPEATABLE', 'APPROVED', 1, true);

-- ============================================================================
-- 三、家务劳动任务 - 培养责任感 (12 个任务)
-- ============================================================================
INSERT INTO tasks (title, description, points, type, status, created_by_id, active) VALUES
('扫地拖地', '打扫客厅或房间地面', 10, 'REPEATABLE', 'APPROVED', 1, true),
('洗碗', '饭后帮忙洗碗', 8, 'REPEATABLE', 'APPROVED', 1, true),
('倒垃圾', '主动倒家里的垃圾', 5, 'REPEATABLE', 'APPROVED', 1, true),
('晾晒衣服', '帮忙晾晒洗好的衣服', 6, 'REPEATABLE', 'APPROVED', 1, true),
('叠衣服', '把晾干的衣服叠好', 6, 'REPEATABLE', 'APPROVED', 1, true),
('擦桌子', '饭后擦拭餐桌', 5, 'REPEATABLE', 'APPROVED', 1, true),
('浇花', '给家里的植物浇水', 4, 'REPEATABLE', 'APPROVED', 1, true),
('喂宠物', '照顾家里的小动物', 6, 'DAILY_ONCE', 'APPROVED', 1, true),
('整理鞋柜', '把鞋子摆放整齐', 5, 'REPEATABLE', 'APPROVED', 1, true),
('帮忙做饭', '协助家长准备饭菜', 12, 'REPEATABLE', 'APPROVED', 1, true),
('超市购物助手', '陪家长购物并帮忙提东西', 10, 'REPEATABLE', 'APPROVED', 1, true),
('照顾弟妹', '帮忙照看弟弟妹妹', 15, 'REPEATABLE', 'APPROVED', 1, true);

-- ============================================================================
-- 四、品德行为任务 - 培养良好品格 (8 个任务)
-- ============================================================================
INSERT INTO tasks (title, description, points, type, status, created_by_id, active) VALUES
('礼貌用语', '主动使用"请"、"谢谢"、"对不起"', 5, 'DAILY_ONCE', 'APPROVED', 1, true),
('尊老爱幼', '主动帮助老人或小朋友', 10, 'REPEATABLE', 'APPROVED', 1, true),
('诚实守信', '说到做到，不撒谎', 10, 'DAILY_ONCE', 'APPROVED', 1, true),
('分享玩具', '愿意和他人分享自己的东西', 8, 'REPEATABLE', 'APPROVED', 1, true),
('公共场合守规矩', '在公共场所保持安静有礼貌', 8, 'REPEATABLE', 'APPROVED', 1, true),
('帮助他人', '主动帮助有需要的人', 12, 'REPEATABLE', 'APPROVED', 1, true),
('承认错误', '做错事后主动承认并道歉', 10, 'REPEATABLE', 'APPROVED', 1, true),
('耐心等待', '排队时不插队不吵闹', 6, 'REPEATABLE', 'APPROVED', 1, true);

-- ============================================================================
-- 五、奖励列表 - 物质和精神奖励 (33 个奖励)
-- ============================================================================
-- 娱乐类奖励
INSERT INTO rewards (name, description, points_required, active) VALUES
('额外游戏时间 30 分钟', '可以额外玩 30 分钟电子游戏或平板', 30, true),
('额外游戏时间 1 小时', '可以额外玩 1 小时电子游戏或平板', 50, true),
('看电影', '选择一部喜欢的电影观看', 40, true),
('去游乐场', '周末去游乐场玩半天', 150, true),
('去动物园', '周末去动物园游玩', 200, true),
('去科技馆', '参观科技馆或博物馆', 120, true);

-- 食物类奖励
INSERT INTO rewards (name, description, points_required, active) VALUES
('冰淇淋', '选择喜欢的冰淇淋一份', 20, true),
('奶茶/果汁', '购买一杯喜欢的饮品', 25, true),
('披萨大餐', '全家一起吃披萨', 100, true),
('肯德基/麦当劳', '去吃一次快餐', 80, true),
('蛋糕甜点', '选择喜欢的蛋糕或甜点', 35, true),
('零食大礼包', '获得一份零食大礼包', 60, true);

-- 物品类奖励
INSERT INTO rewards (name, description, points_required, active) VALUES
('新玩具', '购买一个喜欢的玩具（200 元以内）', 500, true),
('图书', '购买一本喜欢的图书', 40, true),
('文具套装', '获得一套新文具', 50, true),
('新衣服', '购买一件新衣服', 150, true),
('运动鞋', '购买一双新运动鞋', 200, true),
('乐高积木', '购买一套乐高积木', 300, true);

-- 特权类奖励
INSERT INTO rewards (name, description, points_required, active) VALUES
('选择周末活动', '决定周末全家去哪里玩', 80, true),
('选择晚餐菜单', '决定今晚吃什么', 30, true),
('晚睡 1 小时特权', '周末可以晚睡 1 小时', 40, true),
('免做家务一次', '可以免除一次家务任务', 25, true),
('邀请朋友来玩', '邀请好朋友来家里玩', 60, true),
('去朋友家玩', '去好朋友家玩半天', 50, true);

-- 学习类奖励
INSERT INTO rewards (name, description, points_required, active) VALUES
('新画笔/画具', '购买一套绘画工具', 70, true),
('科学实验套装', '获得一个科学实验玩具', 100, true),
('地球仪', '获得一个地球仪', 80, true);

-- 特别奖励
INSERT INTO rewards (name, description, points_required, active) VALUES
('游乐园一日游', '去大型游乐园玩一整天', 500, true),
('露营体验', '周末去郊外露营', 400, true),
('游泳馆', '去游泳馆游泳', 80, true),
('滑冰/滑雪', '去滑冰场或滑雪场', 150, true),
('儿童摄影', '拍一套个人写真', 300, true),
('生日派对', '举办一个小型生日派对', 600, true);

-- ============================================================================
-- 数据验证查询
-- ============================================================================
-- 统计任务总数
SELECT '=== 任务统计 ===' AS info;
SELECT type AS 任务类型，COUNT(*) AS 数量 FROM tasks GROUP BY type;

-- 统计奖励总数
SELECT '=== 奖励统计 ===' AS info;
SELECT 
    CASE 
        WHEN points_required <= 30 THEN '低档 (≤30 分)'
        WHEN points_required <= 100 THEN '中档 (31-100 分)'
        WHEN points_required <= 300 THEN '高档 (101-300 分)'
        ELSE '特档 (>300 分)'
    END AS 奖励档次，
    COUNT(*) AS 数量 
FROM rewards 
GROUP BY 
    CASE 
        WHEN points_required <= 30 THEN '低档 (≤30 分)'
        WHEN points_required <= 100 THEN '中档 (31-100 分)'
        WHEN points_required <= 300 THEN '高档 (101-300 分)'
        ELSE '特档 (>300 分)'
    END;

-- 总览
SELECT '=== 总览 ===' AS info;
SELECT 
    (SELECT COUNT(*) FROM tasks) AS 总任务数，
    (SELECT COUNT(*) FROM rewards) AS 总奖励数;
