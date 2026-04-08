# JPA 实体类与数据库字段对比报告

## 已发现的不匹配问题

### 1. reward_redemptions 表 ❌
**实体类字段:**
- `note` (String, length 500)

**数据库字段:**
- 缺少 `note` 列

**解决方案:** 添加 `note` 列

---

### 2. task_jobs 表 ❌
**实体类字段:**
- `startedAt` (LocalDateTime)
- `deadline` (LocalDateTime)
- `updatedAt` (LocalDateTime)

**数据库字段:**
- 缺少 `started_at`, `deadline`, `updated_at` 列

**解决方案:** 添加这些列

---

### 3. point_history 表 ❌
**实体类字段:**
- `originalPoints` (Integer)
- `changePoints` (Integer)
- `afterPoints` (Integer)

**数据库字段:**
- `change_amount` (int)
- `balance_after` (int)
- 缺少 `original_points` 列

**解决方案:** 
- 方案 A: 修改实体类使用数据库字段名
- 方案 B: 添加缺失列并使用 `@Column` 映射

---

### 4. lottery_draws 表 ❌
**实体类字段:**
- `pointsCost` (Integer)
- `drawAt` (LocalDateTime)

**数据库字段:**
- `points_spent` (int)
- `draw_time` (datetime)

**解决方案:** 使用 `@Column` 指定正确的列名

---

### 5. notifications 表 ❌❌ (严重不匹配)
**实体类字段:**
- `title` (String, 200)
- `content` (String, 1000)
- `createdById` (Long)
- `targetType` (Enum)
- `targetChildId` (Long)
- `priority` (Enum)
- `active` (Boolean)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

**数据库字段:**
- `user_id` (bigint)
- `title` (varchar, 200) ✓
- `message` (text) ← 实体类是 `content`
- `type` (enum) ← 实体类是通知类型，数据库可能是目标类型
- `is_read` (bit) ← 实体类没有
- `created_by_id` (bigint) ✓
- `created_at` (datetime) ✓
- `read_at` (datetime) ← 实体类没有

**解决方案:** 这是严重的结构不匹配，需要决定使用哪个版本

---

### 6. coupons 表 ❌❌ (严重不匹配)
**实体类字段:**
- `code` (String, 100)
- `points` (Integer)
- `enabled` (Boolean)
- `comment` (String)
- `username` (String)
- `timeoutSeconds` (Integer)
- `usedCount` (Integer)
- `redeemed` (Boolean)
- `redeemedBy` (Child)
- `redeemedAt` (LocalDateTime)
- `createdBy` (User)
- `createdAt` (LocalDateTime)
- `insertedAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

**数据库字段:**
- `code` (varchar, 50) ✓
- `description` (varchar, 500) ← 实体类没有
- `comment` (varchar, 500) ✓
- `discount_type` (enum) ← 实体类没有
- `discount_value` (int) ← 实体类没有
- `min_points_required` (int) ← 实体类没有
- `max_discount` (int) ← 实体类没有
- `valid_from` (datetime) ← 实体类没有
- `valid_until` (datetime) ← 实体类没有
- `usage_limit` (int) ← 实体类没有
- `used_count` (int) ✓
- `active` (bit) ← 实体类是 `enabled`
- `created_at` (datetime) ✓
- `updated_at` (datetime) ✓

**解决方案:** 这是完全不同的两个设计，需要统一

---

### 7. lottery_draw_results 表 ⚠️
**实体类字段:**
- `createdAt` (LocalDateTime)

**数据库字段:**
- `won_at` (datetime)

**解决方案:** 使用 `@Column(name = "won_at")` 映射

---

### 8. task_completions 表 ⚠️
**实体类字段:**
- `proof` (String, TEXT)

**数据库字段:**
- `proof` (varchar, 2000)

**解决方案:** 可能需要改为 TEXT 类型或保持现状

---

## 已修复的问题

### ✅ rewards 表
- `image_url` 列已通过 003-add-rewards-image-url.sql 添加

---

## 建议的修复优先级

### P0 - 立即修复（导致运行时错误）
1. **reward_redemptions** - 添加 `note` 列
2. **task_jobs** - 添加 `started_at`, `deadline`, `updated_at` 列

### P1 - 高优先级（可能导致查询错误）
3. **point_history** - 统一字段名或添加映射
4. **lottery_draws** - 添加 `@Column` 映射
5. **lottery_draw_results** - 添加 `@Column` 映射

### P2 - 中优先级（设计不一致）
6. **notifications** - 需要决定使用哪个设计
7. **coupons** - 需要决定使用哪个设计

---

## 需要确认的问题

1. **notifications 表**: 实体类和数据库是完全不同的设计
   - 实体类：家庭广播通知系统（有优先级、目标类型等）
   - 数据库：用户个人通知系统（有已读/未读状态）
   - **建议**: 保留数据库设计，修改实体类

2. **coupons 表**: 实体类和数据库是完全不同的设计
   - 实体类：一次性兑换码系统
   - 数据库：优惠券折扣系统
   - **建议**: 保留数据库设计，修改实体类

3. **point_history 表**: 字段命名不一致
   - 实体类使用驼峰命名
   - 数据库使用下划线命名但字段不同
   - **建议**: 修改实体类匹配数据库
