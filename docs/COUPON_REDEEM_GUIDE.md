# 上网券兑换流程指南

## 📋 完整兑换流程

### 1. 家长准备阶段

#### 1.1 创建上网券礼物（Reward）
```
访问：/parent/rewards
操作：创建礼物
要求：
  - 名称必须包含关键词：上网券、上网、coupon、internet
  - 例如："上网券"、"2 小时上网券"、"Internet Coupon"
  - 设置所需积分：例如 100 积分
```

#### 1.2 导入券码池（Coupon）
```
访问：/parent/coupons
操作：批量导入券码
文件格式 (coupon.txt)：
  id=1 enabled=yes comment= timeout=7200
  id=2 enabled=yes comment= timeout=3600
  id=3 enabled=yes comment= username=child1 timeout=1800

字段说明：
  - id: 券码 ID
  - enabled: 是否启用 (yes/no)
  - comment: 备注
  - username: 指定给特定小孩（可选，留空表示所有小孩可用）
  - timeout: 上网时长（秒）
```

### 2. 小孩兑换阶段

#### 2.1 访问礼物商城
```
URL: /child/rewards
显示：表格形式显示所有可兑换礼物
```

#### 2.2 找到上网券
```
在表格中找到"上网券"礼物
检查：
  - 积分是否足够
  - 库存是否 > 0
  - 状态是否激活
```

#### 2.3 点击兑换
```
点击"兑换"按钮
确认弹窗 → 确定
等待兑换成功提示
```

#### 2.4 查看券码
```
访问：/child/rewards
查看"已兑换礼物"区域
点击已兑换的上网券
显示券码详情：
  【上网券】
  券码：COUPON_001
  时长：2 小时
  兑换时间：2024-01-01 12:00
```

## 🔍 排查清单

### 问题 1：找不到上网券礼物
```
检查项：
□ 家长是否创建了 Reward？
□ Reward 名称是否包含关键词（上网券、上网、coupon、internet）？
□ Reward 是否激活（active=true）？
□ Reward 库存是否 > 0？

解决：
1. 访问 /parent/rewards 创建礼物
2. 名称改为"上网券"或包含关键词
3. 确保库存充足
```

### 问题 2：兑换失败 - 暂无可用上网券
```
检查项：
□ 是否导入了 Coupon 券码？
□ 券码是否启用（enabled=true）？
□ 券码是否已被兑换（redeemed=false）？

解决：
1. 访问 /parent/coupons 导入券码
2. 确保券码 enabled=yes
3. 检查券码池是否有剩余
```

### 问题 3：兑换失败 - 券码指定给其他用户
```
检查项：
□ 券码是否指定了 username？
□ username 是否与当前小孩匹配？

解决：
1. 导入券码时留空 username（所有小孩可用）
2. 或设置 username 为当前小孩的用户名
```

### 问题 4：积分不足
```
检查项：
□ 小孩当前积分是否 >= 礼物所需积分？

解决：
1. 让孩子多做任务赚积分
2. 或降低礼物所需积分
```

## 🧪 测试数据

### 创建测试礼物
```json
POST /api/v1/rewards
{
  "title": "上网券",
  "description": "2 小时上网时长",
  "points": 100
}
```

### 导入测试券码
```bash
# 创建 coupon.txt
cat > coupon.txt << EOF
id=1 enabled=yes comment=测试券 1 timeout=7200
id=2 enabled=yes comment=测试券 2 timeout=3600
id=3 enabled=yes comment=测试券 3 timeout=1800
EOF

# 通过页面导入或 API
POST /api/v1/coupons/import
Form-data: file=@coupon.txt
```

### 测试兑换
```bash
# 小孩登录
# 访问 /child/rewards
# 点击上网券的"兑换"按钮

# 或通过 API
POST /api/v1/rewards/{rewardId}/redeem
Authorization: Bearer {child_token}
```

### 查看结果
```bash
# 查看兑换记录
GET /api/v1/rewards/history

# 响应示例
{
  "code": "SUCCESS",
  "data": [
    {
      "id": 1,
      "rewardName": "上网券",
      "note": "【上网券】\n券码：COUPON_001\n时长：2 小时\n兑换时间：2024-01-01T12:00:00",
      "redeemedAt": "2024-01-01T12:00:00"
    }
  ]
}
```

## 🔧 常见问题

### Q1: 为什么兑换后看不到券码？
**A:** 检查 RewardRedemption.note 字段是否有值
```sql
SELECT id, reward_id, note, redeemed_at 
FROM reward_redemptions 
ORDER BY id DESC LIMIT 5;
```

### Q2: 如何确认 CouponRedeemService 被调用？
**A:** 查看应用日志
```bash
# 搜索关键词
grep "CouponRedeemService" logs/creditapp.log
grep "redeeming coupon" logs/creditapp.log
```

### Q3: 券码池为空怎么办？
**A:** 重新导入券码
```bash
# 检查券码池
SELECT COUNT(*) FROM coupons WHERE enabled=true AND redeemed=false;

# 如果为 0，导入新券码
```

### Q4: 如何调试兑换流程？
**A:** 启用 DEBUG 日志
```properties
# application.properties
logging.level.com.creditapp.service=DEBUG
```

## 📊 数据库表关系

```
rewards (礼物定义)
  ├── id
  ├── name: "上网券"
  ├── points_required: 100
  └── quantity: 999

coupons (券码池 - 独立管理)
  ├── id
  ├── code: "COUPON_001"
  ├── timeout_seconds: 7200
  ├── enabled: true
  ├── redeemed: false
  └── redeemed_by: NULL

reward_redemptions (兑换记录)
  ├── id
  ├── reward_id → rewards.id
  ├── child_id → children.id
  ├── note: "【上网券】\n券码：COUPON_001..."
  └── redeemed_at
```

## 🎯 关键代码位置

1. **兑换入口**: `RewardController.redeemReward()`
2. **策略管理**: `RedemptionStrategyManager.redeem()`
3. **上网券服务**: `CouponRedeemService.redeem()`
4. **券码分配**: `CouponRepository.findByEnabledTrueAndRedeemedFalse()`
5. **结果填充**: `RewardServiceImpl.redeemReward()` → `redemption.setNote()`
