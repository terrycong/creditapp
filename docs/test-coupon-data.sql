-- =====================================================
-- 上网券兑换流程测试数据
-- 执行此脚本创建测试用的礼物和券码
-- =====================================================

-- 1. 创建上网券礼物（Reward）
INSERT INTO rewards (name, description, points_required, quantity, active, image_url) 
VALUES (
  '上网券', 
  '2 小时上网时长，兑换后在已兑换礼物中查看券码', 
  100, 
  999, 
  TRUE,
  'https://images.unsplash.com/photo-1544197150-b99a580bbcbf?w=400'
);

-- 2. 导入券码池（Coupon）
-- 注意：需要先知道 created_by 的用户 ID（通常是家长用户）
-- 假设家长用户 ID = 1，请根据实际情况修改

INSERT INTO coupons (code, enabled, comment, username, timeout_seconds, used_count, redeemed, created_by, created_at) VALUES
('COUPON_TEST_001', TRUE, '测试券码 1 - 2 小时', NULL, 7200, 0, FALSE, 1, NOW()),
('COUPON_TEST_002', TRUE, '测试券码 2 - 2 小时', NULL, 7200, 0, FALSE, 1, NOW()),
('COUPON_TEST_003', TRUE, '测试券码 3 - 1 小时', NULL, 3600, 0, FALSE, 1, NOW()),
('COUPON_TEST_004', TRUE, '测试券码 4 - 30 分钟', NULL, 1800, 0, FALSE, 1, NOW()),
('COUPON_TEST_005', TRUE, '测试券码 5 - 指定给 child1', 'child1', 7200, 0, FALSE, 1, NOW());

-- 3. 验证数据
SELECT '=== 已创建的上网券礼物 ===' as info;
SELECT id, name, points_required, quantity, active FROM rewards WHERE name LIKE '%上网%';

SELECT '=== 已导入的券码池 ===' as info;
SELECT id, code, timeout_seconds, enabled, redeemed, username FROM coupons WHERE code LIKE 'COUPON_TEST%';

SELECT '=== 检查策略服务 ===' as info;
SELECT 'CouponRedeemService 应该自动被 Spring 扫描并注册' as note;

-- =====================================================
-- 测试步骤：
-- 1. 执行此 SQL 脚本
-- 2. 小孩登录（确保积分 >= 100）
-- 3. 访问 /child/rewards
-- 4. 找到"上网券"礼物，点击兑换
-- 5. 查看兑换记录中的券码信息
-- =====================================================
