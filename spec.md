# CreditApp 规格说明书 (SDD 版)

> 家庭积分任务管理系统 - Spec-Driven Development 规格文档  
> 版本：1.4.2 | 最后更新：2026-04-17  
> 用途：驱动开发、测试、验收的单一事实来源

---

## 📋 目录

1. [产品概述](#1-产品概述)
2. [用户角色与权限](#2-用户角色与权限)
3. [功能规格详述](#3-功能规格详述)
4. [API 规范](#4-api 规范)
5. [数据模型](#5-数据模型)
6. [业务流程](#6-业务流程)
7. [状态机定义](#7-状态机定义)
8. [验证规则](#8-验证规则)
9. [错误处理](#9-错误处理)
10. [非功能需求](#10-非功能需求)
11. [测试场景](#11-测试场景)

---

## 1. 产品概述

### 1.1 产品愿景
构建一个透明的家庭积分管理系统，通过任务 - 奖励机制培养儿童良好行为习惯，增强亲子互动。

### 1.2 核心价值主张
| 价值点 | 说明 |
|--------|------|
| 透明激励 | 儿童清楚知道每个任务的价值和奖励 |
| 即时反馈 | 任务完成后即时获得积分，强化正向行为 |
| 自主选择 | 儿童可自主选择任务和奖励，培养决策能力 |
| 家庭互动 | 通过任务审核、奖励兑换增强亲子沟通 |

### 1.3 用户画像

#### 家长（Parent）
- **年龄**：30-45 岁
- **痛点**：孩子行为习惯培养困难，缺乏有效激励手段
- **目标**：建立规则意识，培养良好习惯
- **使用场景**：创建任务、审核完成、管理奖励、查看报告

#### 儿童（Child）
- **年龄**：6-12 岁
- **痛点**：不理解行为后果，缺乏动力
- **目标**：获得积分，兑换喜欢的奖励
- **使用场景**：查看任务、提交完成、兑换奖励、参与抽奖

### 1.4 术语表

| 术语 | 定义 |
|------|------|
| 任务（Task） | 家长创建的行为要求，完成后获得积分 |
| 任务实例（TaskJob） | 任务分配给具体儿童后生成的实例 |
| 任务完成（TaskCompletion） | 儿童提交的任务完成记录 |
| 奖励（Reward） | 可用积分兑换的物品或特权 |
| 抽奖（Lottery） | 消耗积分参与的概率性奖励获取 |
| 惩罚（Penalty） | 违规行为导致的积分扣除 |
| 积分（Points） | 系统内虚拟货币，用于衡量行为价值 |

---

## 2. 用户角色与权限

### 2.1 角色定义

#### PARENT（家长）
- 系统管理员角色
- 可创建和管理所有资源
- 可查看所有儿童数据

#### CHILD（儿童）
- 受限用户角色
- 仅能操作自己的数据
- 需要家长审核关键操作

### 2.2 权限矩阵

| 功能模块 | 操作 | PARENT | CHILD |
|----------|------|--------|-------|
| **任务管理** | 创建任务 | ✅ | ❌ |
| | 编辑任务 | ✅ (所有) | ❌ |
| | 删除任务 | ✅ (所有) | ❌ |
| | 查看任务 | ✅ (所有) | ✅ (自己的) |
| | 提交完成 | ❌ | ✅ |
| | 审核完成 | ✅ | ❌ |
| | 挑选任务 | ❌ | ✅ |
| **奖励管理** | 创建奖励 | ✅ | ❌ |
| | 编辑奖励 | ✅ | ❌ |
| | 删除奖励 | ✅ | ❌ |
| | 查看奖励 | ✅ | ✅ |
| | 兑换奖励 | ❌ | ✅ |
| | 标记已使用 | ✅ | ❌ |
| **抽奖** | 创建主题 | ✅ | ❌ |
| | 创建奖品 | ✅ | ❌ |
| | 参与抽奖 | ❌ | ✅ |
| | 查看历史 | ✅ (所有) | ✅ (自己的) |
| **惩罚** | 创建规则 | ✅ | ❌ |
| | 执行惩罚 | ✅ | ❌ |
| | 查看记录 | ✅ (所有) | ✅ (自己的) |
| **反馈** | 提交反馈 | ✅ | ✅ |
| | 回复反馈 | ✅ | ❌ |
| **通知** | 创建通知 | ✅ | ❌ |
| | 查看通知 | ✅ | ✅ |
| | 标记已读 | ✅ | ✅ |
| **系统** | 数据库备份 | ✅ | ❌ |
| | 用户管理 | ✅ | ❌ |

### 2.3 数据隔离规则

```
CHILD 用户只能访问：
- 自己的积分 (Child.points)
- 分配给自己的任务实例 (TaskJob.child_id = current_user.id)
- 自己的完成记录 (TaskCompletion.child_id = current_user.id)
- 自己的奖励兑换 (RewardRedemption.child_id = current_user.id)
- 自己的抽奖历史 (LotteryDraw.child_id = current_user.id)
- 自己的惩罚记录 (PenaltyRecord.child_id = current_user.id)
- 自己的反馈 (Feedback.child_id = current_user.id)

PARENT 用户可以访问：
- 所有数据（无限制）
```

---

## 3. 功能规格详述

### 3.1 任务管理模块

#### 3.1.1 创建任务

**用户故事**
> 作为家长，我希望创建任务并设置积分奖励，以便激励孩子养成良好习惯。

**前置条件**
- 用户已登录且角色为 PARENT
- 任务标题不为空

**输入字段**

| 字段 | 类型 | 必填 | 验证规则 | 说明 |
|------|------|------|----------|------|
| title | String | 是 | 1-100 字符 | 任务标题 |
| description | String | 否 | 0-500 字符 | 任务描述 |
| points | Integer | 是 | 1-999 | 奖励积分 |
| type | Enum | 是 | DAILY_ONCE\|MANDATORY\|ONE_TIME\|REPEATABLE | 任务类型 |
| deadlineType | Enum | 条件 | DAILY\|WEEKLY_TIMES (仅 MANDATORY) | 截止时间类型 |
| deadlineValue | Integer | 条件 | 1-99 (仅 MANDATORY) | 截止时间值 |
| penaltyPoints | Integer | 条件 | 1-999 (仅 MANDATORY) | 未完成惩罚积分 |
| active | Boolean | 否 | 默认 true | 是否启用 |

**处理逻辑**
1. 验证输入字段
2. 设置 createdBy = 当前用户 ID
3. 设置 status = APPROVED
4. 设置 createdAt = 当前时间
5. 保存到 tasks 表
6. 返回创建的任务 DTO

**输出**
```json
{
  "success": true,
  "message": "任务创建成功",
  "data": {
    "id": 34,
    "title": "不赖床",
    "description": "按时起床不赖床",
    "points": 5,
    "type": "DAILY_ONCE",
    "status": "APPROVED",
    "active": true,
    "createdAt": "2026-04-17T08:00:00"
  }
}
```

**验收标准**
- [ ] 标题为空时返回错误 "标题不能为空"
- [ ] 积分小于 1 时返回错误 "积分必须大于 0"
- [ ] 积分大于 999 时返回错误 "积分不能超过 999"
- [ ] MANDATORY 类型必须设置 deadlineType 和 deadlineValue
- [ ] 非 MANDATORY 类型设置 deadline 字段时忽略
- [ ] 创建成功后任务状态为 APPROVED
- [ ] 创建者 ID 正确记录

---

#### 3.1.2 完成任务

**用户故事**
> 作为儿童，我希望提交任务完成，以便获得积分奖励。

**前置条件**
- 用户已登录且角色为 CHILD
- 任务存在且状态为 APPROVED
- 任务处于活动状态 (active = true)
- 存在分配给该儿童的任务实例 (TaskJob)
- 任务实例状态为 ASSIGNED 或 IN_PROGRESS

**输入字段**

| 字段 | 类型 | 必填 | 验证规则 | 说明 |
|------|------|------|----------|------|
| taskId | Long | 是 | 任务必须存在 | 任务 ID |
| proof | String | 否 | 0-1000 字符 | 完成证明（可选） |

**处理逻辑**
1. 验证任务存在且 active = true
2. 验证任务实例存在且属于当前儿童
3. 验证任务类型允许完成：
   - DAILY_ONCE: 今天尚未完成
   - MANDATORY: 检查完成次数是否达标
   - ONE_TIME: 尚未完成过
   - REPEATABLE: 无限制
4. 创建 TaskCompletion 记录，status = PENDING
5. 更新 TaskJob 状态 = COMPLETED
6. 返回完成记录 DTO

**输出**
```json
{
  "success": true,
  "message": "任务完成已提交，等待家长审核",
  "data": {
    "id": 24,
    "taskId": 8,
    "childId": 1,
    "status": "PENDING",
    "proof": null,
    "completedAt": "2026-04-17T08:30:00"
  }
}
```

**验收标准**
- [ ] 任务不存在时返回错误 "任务不存在"
- [ ] 任务不活跃时返回错误 "任务已停用"
- [ ] 任务实例不存在时返回错误 "任务未分配给你"
- [ ] DAILY_ONCE 类型今天已完成时返回错误 "今天已完成此任务"
- [ ] 提交成功后状态为 PENDING
- [ ] 家长审核前积分不增加

---

#### 3.1.3 审核任务完成

**用户故事**
> 作为家长，我希望审核孩子提交的任务完成，以便确认是否给予积分。

**前置条件**
- 用户已登录且角色为 PARENT
- 完成记录存在且状态为 PENDING

**输入字段**

| 字段 | 类型 | 必填 | 验证规则 | 说明 |
|------|------|------|----------|------|
| completionId | Long | 是 | 完成记录必须存在 | 完成记录 ID |
| decision | Enum | 是 | APPROVE\|REJECT | 审核决定 |
| rejectionReason | String | 条件 | 0-500 字符 (仅 REJECT) | 拒绝原因 |

**处理逻辑 - 批准**
1. 验证完成记录存在且 status = PENDING
2. 更新 TaskCompletion.status = APPROVED
3. 更新 TaskCompletion.approvedAt = 当前时间
4. 增加儿童积分：Child.points += Task.points
5. 创建 PointHistory 记录：
   - changeType = TASK_COMPLETION
   - changePoints = Task.points
   - originalPoints = 原积分
   - afterPoints = 新积分
   - description = "完成任务：{任务标题}"
   - referenceId = TaskCompletion.id
   - referenceType = "TASK_COMPLETION"
6. 返回完成记录 DTO

**处理逻辑 - 拒绝**
1. 验证完成记录存在且 status = PENDING
2. 更新 TaskCompletion.status = REJECTED
3. 更新 TaskJob.status = ASSIGNED (允许重新提交)
4. 可选：创建 Notification 通知儿童
5. 返回完成记录 DTO

**输出 - 批准**
```json
{
  "success": true,
  "message": "任务完成已批准，积分已发放",
  "data": {
    "id": 24,
    "taskId": 8,
    "childId": 1,
    "status": "APPROVED",
    "approvedAt": "2026-04-17T09:00:00",
    "pointsAwarded": 5
  }
}
```

**输出 - 拒绝**
```json
{
  "success": true,
  "message": "任务完成已拒绝",
  "data": {
    "id": 24,
    "taskId": 8,
    "childId": 1,
    "status": "REJECTED",
    "rejectionReason": "照片不清晰，请重新提交"
  }
}
```

**验收标准**
- [ ] 完成记录不存在时返回错误 "完成记录不存在"
- [ ] 完成记录已审核时返回错误 "该完成记录已审核"
- [ ] 批准后儿童积分正确增加
- [ ] 批准后创建 PointHistory 记录
- [ ] 拒绝后任务实例状态恢复为 ASSIGNED
- [ ] 拒绝后儿童可以重新提交

---

### 3.2 奖励管理模块

#### 3.2.1 创建奖励

**用户故事**
> 作为家长，我希望创建奖励并设置所需积分，以便激励孩子完成任务。

**前置条件**
- 用户已登录且角色为 PARENT

**输入字段**

| 字段 | 类型 | 必填 | 验证规则 | 说明 |
|------|------|------|----------|------|
| name | String | 是 | 1-100 字符 | 奖励名称 |
| description | String | 否 | 0-500 字符 | 奖励描述 |
| pointsRequired | Integer | 是 | 1-999 | 所需积分 |
| quantity | Integer | 否 | 1-999, 默认 999 | 可用数量 |
| imageUrl | String | 否 | 0-500 字符 | 图片 URL |
| active | Boolean | 否 | 默认 true | 是否启用 |

**处理逻辑**
1. 验证输入字段
2. 设置 active = true (默认)
3. 设置 quantity = 999 (默认)
4. 保存到 rewards 表
5. 返回奖励 DTO

**验收标准**
- [ ] 名称为空时返回错误 "奖励名称不能为空"
- [ ] 所需积分小于 1 时返回错误 "积分必须大于 0"
- [ ] 创建成功后奖励状态为 active

---

#### 3.2.2 兑换奖励

**用户故事**
> 作为儿童，我希望用积分兑换奖励，以便获得想要的物品或特权。

**前置条件**
- 用户已登录且角色为 CHILD
- 奖励存在且 active = true
- 奖励数量 > 0 (或 quantity = 999 表示无限)
- 儿童积分 >= 奖励所需积分

**处理逻辑**
1. 验证奖励存在且 active = true
2. 验证奖励数量充足
3. 验证儿童积分充足
4. 扣除儿童积分：Child.points -= Reward.pointsRequired
5. 减少奖励数量：Reward.quantity -= 1
6. 创建 RewardRedemption 记录：
   - status = REDEEMED
   - redeemedAt = 当前时间
7. 创建 PointHistory 记录：
   - changeType = REWARD_REDEMPTION
   - changePoints = -Reward.pointsRequired
   - description = "兑换奖励：{奖励名称}"
   - referenceId = RewardRedemption.id
   - referenceType = "REWARD_REDEMPTION"
8. 返回兑换记录 DTO

**输出**
```json
{
  "success": true,
  "message": "奖励兑换成功",
  "data": {
    "id": 3,
    "rewardId": 14,
    "rewardName": "10 分钟上网券",
    "childId": 1,
    "status": "REDEEMED",
    "pointsUsed": 50,
    "remainingPoints": 70,
    "redeemedAt": "2026-04-17T10:00:00"
  }
}
```

**验收标准**
- [ ] 奖励不存在时返回错误 "奖励不存在"
- [ ] 奖励不活跃时返回错误 "该奖励已停用"
- [ ] 奖励数量不足时返回错误 "奖励库存不足"
- [ ] 积分不足时返回错误 "积分不足"
- [ ] 兑换成功后积分正确扣除
- [ ] 兑换成功后奖励数量正确减少
- [ ] 创建 PointHistory 记录

---

### 3.3 抽奖模块

#### 3.3.1 创建抽奖主题

**用户故事**
> 作为家长，我希望创建抽奖主题和奖品池，以便增加游戏的趣味性。

**前置条件**
- 用户已登录且角色为 PARENT

**输入字段**

| 字段 | 类型 | 必填 | 验证规则 | 说明 |
|------|------|------|----------|------|
| name | String | 是 | 1-100 字符 | 主题名称 |
| description | String | 否 | 0-500 字符 | 主题描述 |
| pointsPerDraw | Integer | 是 | 1-999 | 每次抽奖消耗积分 |
| type | Enum | 是 | FIXED_PROBABILITY\|GUARANTEED\|WEIGHTED_RANDOM | 抽奖类型 |
| active | Boolean | 否 | 默认 true | 是否启用 |

**处理逻辑**
1. 验证输入字段
2. 设置 createdBy = 当前用户 ID
3. 设置 active = true (默认)
4. 保存到 lottery_themes 表
5. 返回主题 DTO

---

#### 3.3.2 执行抽奖

**用户故事**
> 作为儿童，我希望参与抽奖，以便有机会获得额外奖励。

**前置条件**
- 用户已登录且角色为 CHILD
- 抽奖主题存在且 active = true
- 儿童积分 >= 抽奖消耗积分
- 奖品池中有可用奖品

**抽奖算法（WEIGHTED_RANDOM）**
```
1. 获取主题下所有 active = true 的奖品
2. 计算总权重：totalWeight = sum(prize.weight)
3. 生成随机数：random = 0 到 totalWeight-1
4. 遍历奖品，累加权重，找到命中奖品：
   - cumulativeWeight += prize.weight
   - if random < cumulativeWeight: selected = prize
5. 检查奖品数量：
   - 如果 quantity != -1 且 redeemedCount >= quantity: 重新抽奖
6. 增加奖品 redeemedCount
7. 扣除儿童积分
8. 创建 LotteryDraw 和 LotteryDrawResult 记录
9. 如果奖品关联 Reward，创建 RewardRedemption
10. 创建 PointHistory 记录
```

**输出**
```json
{
  "success": true,
  "message": "抽奖成功",
  "data": {
    "drawId": 2,
    "themeId": 2,
    "themeName": "幸运大转盘",
    "pointsCost": 50,
    "resultStatus": "WON",
    "prize": {
      "id": 5,
      "name": "10 分钟上网券",
      "description": "上网 10 分钟",
      "imageUrl": null
    },
    "drawAt": "2026-04-17T11:00:00"
  }
}
```

**验收标准**
- [ ] 主题不存在时返回错误 "抽奖主题不存在"
- [ ] 主题不活跃时返回错误 "该抽奖已停用"
- [ ] 积分不足时返回错误 "积分不足"
- [ ] 奖品池为空时返回错误 "奖品池为空"
- [ ] 抽奖后积分正确扣除
- [ ] 奖品数量正确更新
- [ ] 权重算法正确执行

---

### 3.4 惩罚模块

#### 3.4.1 创建惩罚规则

**用户故事**
> 作为家长，我希望定义惩罚规则，以便对违规行为进行约束。

**输入字段**

| 字段 | 类型 | 必填 | 验证规则 | 说明 |
|------|------|------|----------|------|
| name | String | 是 | 1-100 字符 | 规则名称 |
| description | String | 否 | 0-500 字符 | 规则描述 |
| points | Integer | 是 | 1-999 | 扣分数 |
| active | Boolean | 否 | 默认 true | 是否启用 |

---

#### 3.4.2 执行惩罚

**用户故事**
> 作为家长，我希望对违规行为执行惩罚，以便让孩子认识错误。

**处理逻辑**
1. 验证惩罚规则存在且 active = true
2. 验证儿童存在
3. 扣除儿童积分：Child.points -= PenaltyRule.points
4. 创建 PenaltyRecord 记录
5. 创建 PointHistory 记录：
   - changeType = PENALTY
   - changePoints = -PenaltyRule.points
   - description = "违规扣分：{规则名称} - {备注}"
   - referenceId = PenaltyRecord.id
   - referenceType = "PENALTY"
6. 可选：创建 Notification 通知儿童

**输出**
```json
{
  "success": true,
  "message": "惩罚已执行",
  "data": {
    "id": 4,
    "childId": 1,
    "ruleId": 1,
    "ruleName": "浪费牛奶",
    "pointsDeducted": 5,
    "remainingPoints": 115,
    "note": "浪费牛奶",
    "appliedAt": "2026-04-17T12:00:00"
  }
}
```

**验收标准**
- [ ] 规则不存在时返回错误 "惩罚规则不存在"
- [ ] 规则不活跃时返回错误 "该规则已停用"
- [ ] 惩罚后积分正确扣除（允许负数）
- [ ] 创建 PenaltyRecord 记录
- [ ] 创建 PointHistory 记录

---

### 3.5 反馈模块

#### 3.5.1 提交反馈

**用户故事**
> 作为用户，我希望提交反馈和建议，以便改进系统。

**输入字段**

| 字段 | 类型 | 必填 | 验证规则 | 说明 |
|------|------|------|----------|------|
| category | Enum | 是 | FUNCTIONAL\|NON_FUNCTIONAL\|OTHER | 反馈分类 |
| title | String | 是 | 1-200 字符 | 反馈标题 |
| description | String | 是 | 1-2000 字符 | 反馈详情 |

**处理逻辑**
1. 验证输入字段
2. 设置 childId = 当前用户 ID (CHILD) 或关联的儿童 ID (PARENT)
3. 设置 status = PENDING
4. 保存到 feedbacks 表
5. 返回反馈 DTO

---

### 3.6 通知模块

#### 3.6.1 创建通知

**用户故事**
> 作为家长，我希望发送通知给孩子，以便传达重要信息。

**输入字段**

| 字段 | 类型 | 必填 | 验证规则 | 说明 |
|------|------|------|----------|------|
| title | String | 是 | 1-200 字符 | 通知标题 |
| content | String | 是 | 1-1000 字符 | 通知内容 |
| targetType | Enum | 是 | ALL\|SPECIFIC_CHILD | 目标类型 |
| targetChildId | Long | 条件 | (仅 SPECIFIC_CHILD) | 目标儿童 ID |
| priority | Enum | 否 | LOW\|NORMAL\|HIGH\|URGENT, 默认 NORMAL | 优先级 |

---

### 3.7 优惠券模块

#### 3.7.1 创建优惠券

**用户故事**
> 作为家长，我希望创建上网时间优惠券，以便灵活管理孩子的上网时间。

**输入字段**

| 字段 | 类型 | 必填 | 验证规则 | 说明 |
|------|------|------|----------|------|
| code | String | 是 | 1-100 字符，唯一 | 优惠券代码 |
| points | Integer | 是 | 1-999 | 积分值 |
| timeoutSeconds | Integer | 是 | 60-86400 | 有效期（秒） |
| username | String | 否 | 0-50 字符 | 指定用户（可选） |
| comment | String | 否 | 0-500 字符 | 备注 |

---

### 3.8 数据库备份模块

#### 3.8.1 执行备份

**功能描述**
- 定期自动备份数据库
- 保留最近 30 天的备份
- 支持手动触发备份

**配置项**
```properties
database.backup.enabled=true
database.backup.dir=./backups
database.backup.retention-days=30
```

---

## 4. API 规范

### 4.1 通用规范

#### 4.1.1 响应格式

**成功响应**
```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2026-04-17T12:00:00.000+08:00"
}
```

**错误响应**
```json
{
  "success": false,
  "error": "ERROR_CODE",
  "message": "错误描述",
  "details": [
    {
      "field": "fieldName",
      "message": "具体错误信息"
    }
  ],
  "timestamp": "2026-04-17T12:00:00.000+08:00"
}
```

#### 4.1.2 分页响应
```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "pageable": {
      "page": 0,
      "size": 20,
      "sort": "createdAt,desc"
    },
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

#### 4.1.3 认证要求
- 所有 API 需要认证（除登录、注册）
- 使用 Session 认证
- 未认证返回 401 Unauthorized

---

### 4.2 认证 API

#### POST /auth/login
**请求**
```json
{
  "username": "parent",
  "password": "parent123"
}
```

**响应（成功）**
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "parent",
    "role": "PARENT",
    "points": 0
  }
}
```

**响应（失败）**
```json
{
  "success": false,
  "error": "LOGIN_FAILED",
  "message": "用户名或密码错误"
}
```

---

#### POST /auth/logout
**响应**
```json
{
  "success": true,
  "message": "登出成功"
}
```

---

#### GET /auth/current-user
**响应**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "username": "parent",
    "role": "PARENT",
    "points": 0
  }
}
```

---

### 4.3 任务 API

#### POST /api/v1/tasks
**请求**
```json
{
  "title": "不赖床",
  "description": "按时起床不赖床",
  "points": 5,
  "type": "DAILY_ONCE",
  "active": true
}
```

**响应**
```json
{
  "success": true,
  "message": "任务创建成功",
  "data": {
    "id": 34,
    "title": "不赖床",
    "description": "按时起床不赖床",
    "points": 5,
    "type": "DAILY_ONCE",
    "status": "APPROVED",
    "active": true,
    "createdAt": "2026-04-17T08:00:00"
  }
}
```

---

#### GET /api/v1/tasks
**查询参数**
| 参数 | 类型 | 说明 |
|------|------|------|
| type | Enum | 按类型过滤 |
| status | Enum | 按状态过滤 |
| active | Boolean | 按活跃状态过滤 |
| page | Integer | 页码（默认 0） |
| size | Integer | 每页大小（默认 20） |

**响应**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 8,
        "title": "不赖床",
        "points": 5,
        "type": "DAILY_ONCE",
        "active": true
      }
    ],
    "totalElements": 26,
    "totalPages": 2
  }
}
```

---

#### POST /api/v1/tasks/{id}/complete
**请求**
```json
{
  "proof": "已完成"
}
```

**响应**
```json
{
  "success": true,
  "message": "任务完成已提交，等待家长审核",
  "data": {
    "id": 24,
    "taskId": 8,
    "status": "PENDING",
    "completedAt": "2026-04-17T08:30:00"
  }
}
```

---

#### POST /api/v1/tasks/approvals/{completionId}
**响应**
```json
{
  "success": true,
  "message": "任务完成已批准，积分已发放",
  "data": {
    "id": 24,
    "status": "APPROVED",
    "approvedAt": "2026-04-17T09:00:00",
    "pointsAwarded": 5
  }
}
```

---

#### POST /api/v1/tasks/rejections/{completionId}
**请求**
```json
{
  "reason": "照片不清晰"
}
```

**响应**
```json
{
  "success": true,
  "message": "任务完成已拒绝",
  "data": {
    "id": 24,
    "status": "REJECTED",
    "rejectionReason": "照片不清晰"
  }
}
```

---

### 4.4 奖励 API

#### POST /api/v1/rewards
**请求**
```json
{
  "name": "10 分钟上网券",
  "description": "上网 10 分钟",
  "pointsRequired": 50,
  "quantity": 100
}
```

---

#### POST /api/v1/rewards/{id}/redeem
**响应**
```json
{
  "success": true,
  "message": "奖励兑换成功",
  "data": {
    "id": 3,
    "rewardName": "10 分钟上网券",
    "pointsUsed": 50,
    "remainingPoints": 70,
    "status": "REDEEMED"
  }
}
```

---

### 4.5 抽奖 API

#### POST /api/v1/lottery/themes
**请求**
```json
{
  "name": "幸运大转盘",
  "description": "每日抽奖机会",
  "pointsPerDraw": 50,
  "type": "WEIGHTED_RANDOM"
}
```

---

#### POST /api/v1/lottery/themes/{id}/draw
**响应**
```json
{
  "success": true,
  "message": "抽奖成功",
  "data": {
    "drawId": 2,
    "themeName": "幸运大转盘",
    "pointsCost": 50,
    "prize": {
      "name": "10 分钟上网券",
      "description": "上网 10 分钟"
    },
    "resultStatus": "WON"
  }
}
```

---

### 4.6 惩罚 API

#### POST /api/v1/penalties/rules
**请求**
```json
{
  "name": "浪费牛奶",
  "description": "浪费牛奶",
  "points": 5
}
```

---

#### POST /api/v1/penalties/apply
**请求**
```json
{
  "childId": 1,
  "ruleId": 1,
  "note": "浪费牛奶"
}
```

**响应**
```json
{
  "success": true,
  "message": "惩罚已执行",
  "data": {
    "id": 4,
    "ruleName": "浪费牛奶",
    "pointsDeducted": 5,
    "remainingPoints": 115
  }
}
```

---

### 4.7 反馈 API

#### POST /api/v1/feedback
**请求**
```json
{
  "category": "FUNCTIONAL",
  "title": "希望能有更多奖励选择",
  "description": "现在的奖励有点少，希望能增加一些新的奖励"
}
```

---

#### PUT /api/v1/feedback/{id}/response
**请求**
```json
{
  "status": "ACCEPTED",
  "response": "好的，我们会增加更多奖励选项"
}
```

---

### 4.8 通知 API

#### POST /api/v1/notifications
**请求**
```json
{
  "title": "欢迎来到积分系统",
  "content": "欢迎来到积分系统",
  "targetType": "ALL",
  "priority": "NORMAL"
}
```

---

#### POST /api/v1/notifications/{id}/read
**响应**
```json
{
  "success": true,
  "message": "通知已标记为已读"
}
```

---

### 4.9 积分历史 API

#### GET /api/v1/points/history
**查询参数**
| 参数 | 类型 | 说明 |
|------|------|------|
| childId | Long | 儿童 ID（家长可指定） |
| changeType | Enum | 按变动类型过滤 |
| startDate | Date | 开始日期 |
| endDate | Date | 结束日期 |
| page | Integer | 页码 |
| size | Integer | 每页大小 |

**响应**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 27,
        "changeType": "PENALTY",
        "changePoints": -5,
        "originalPoints": 125,
        "afterPoints": 120,
        "description": "违规扣分：浪费牛奶",
        "createdAt": "2026-04-14T14:20:12"
      }
    ],
    "totalElements": 27
  }
}
```

---

### 4.10 仪表板 API

#### GET /api/v1/dashboard/stats
**响应**
```json
{
  "success": true,
  "data": {
    "totalTasks": 26,
    "activeTasks": 25,
    "completedToday": 5,
    "pendingApprovals": 2,
    "totalRewards": 37,
    "childPoints": 120,
    "weeklyCompletionRate": 0.85
  }
}
```

---

## 5. 数据模型

### 5.1 实体关系图

```
┌─────────────────────────────────────────────────────────────────────┐
│                           CORE ENTITIES                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────┐         ┌──────────┐         ┌──────────┐            │
│  │   User   │◄────────│  Child   │────────►│  Reward  │            │
│  └────┬─────┘         └────┬─────┘         └────┬─────┘            │
│       │                    │                    │                   │
│       │ 1:N                │ 1:N                │ 1:N               │
│       ▼                    ▼                    ▼                   │
│  ┌──────────┐         ┌──────────┐         ┌──────────────┐        │
│  │   Task   │         │ TaskJob  │         │RewardRedemp. │        │
│  └────┬─────┘         └────┬─────┘         └──────────────┘        │
│       │                    │                                       │
│       │ 1:N                │ 1:N                                   │
│       ▼                    ▼                                       │
│  ┌──────────────┐    ┌──────────────┐                              │
│  │TaskCompletion│    │TaskDeadline  │                              │
│  └──────────────┘    └──────────────┘                              │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│                        LOTTERY ENTITIES                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────┐         ┌──────────────┐                         │
│  │LotteryTheme  │────────►│LotteryPrize  │                         │
│  └──────┬───────┘         └──────────────┘                         │
│         │ 1:N                                                      │
│         ▼                                                          │
│  ┌──────────────┐         ┌──────────────┐                         │
│  │ LotteryDraw  │────────►│LotteryDrawRes│                         │
│  └──────────────┘         └──────────────┘                         │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│                       PENALTY ENTITIES                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────┐         ┌──────────────┐                         │
│  │PenaltyRule   │────────►│PenaltyRecord │                         │
│  └──────────────┘         └──────────────┘                         │
│                                                                     │
│  ┌──────────────┐                                                  │
│  │PenaltyNotif. │                                                  │
│  └──────────────┘                                                  │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│                      SUPPORTING ENTITIES                            │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │PointHistory  │  │Notification  │  │   Feedback   │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
│                                                                     │
│  ┌──────────────┐  ┌──────────────┐                                 │
│  │   Coupon     │  │Notif.Prefer. │                                 │
│  └──────────────┘  └──────────────┘                                 │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 核心实体详述

#### User（用户）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| username | String | NOT NULL, UNIQUE(50) | 用户名 |
| password | String | NOT NULL(255) | BCrypt 加密密码 |
| role | Enum | NOT NULL | PARENT\|CHILD |
| points | Integer | NOT NULL, DEFAULT 0 | 积分 |
| parent_id | Long | FK → User.id | 家长 ID（儿童用） |

#### Child（儿童）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| username | String | NOT NULL, UNIQUE(50) | 用户名 |
| password | String | NOT NULL(255) | BCrypt 加密密码 |
| role | Enum | NOT NULL | CHILD |
| points | Integer | NOT NULL, DEFAULT 0 | 积分 |
| parent_id | Long | FK → User.id, NOT NULL | 家长 ID |

#### Task（任务）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| title | String | NOT NULL(100) | 任务标题 |
| description | String | (500) | 任务描述 |
| points | Integer | NOT NULL | 奖励积分 |
| type | Enum | NOT NULL | DAILY_ONCE\|MANDATORY\|ONE_TIME\|REPEATABLE |
| status | Enum | | DRAFT\|APPROVED\|REJECTED |
| active | Boolean | NOT NULL, DEFAULT true | 是否启用 |
| created_by_id | Long | FK → User.id | 创建者 |
| created_at | DateTime | NOT NULL | 创建时间 |
| updated_at | DateTime | | 更新时间 |
| deadline_type | Enum | | DAILY\|WEEKLY_TIMES |
| deadline_value | Integer | | 截止时间值 |
| penalty_points | Integer | | 未完成惩罚积分 |

#### TaskJob（任务实例）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| task_id | Long | FK → Task.id, NOT NULL | 任务 ID |
| child_id | Long | FK → Child.id, NOT NULL | 儿童 ID |
| status | Enum | NOT NULL | ASSIGNED\|IN_PROGRESS\|COMPLETED\|CANCELLED |
| snapshot_title | String | NOT NULL(100) | 任务标题快照 |
| snapshot_description | String | (500) | 任务描述快照 |
| snapshot_points | Integer | NOT NULL | 积分快照 |
| snapshot_task_type | Enum | NOT NULL | 任务类型快照 |
| assigned_at | DateTime | NOT NULL | 分配时间 |
| started_at | DateTime | | 开始时间 |
| deadline | DateTime | | 截止时间 |
| created_at | DateTime | NOT NULL | 创建时间 |
| updated_at | DateTime | | 更新时间 |

#### TaskCompletion（任务完成）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| task_id | Long | FK → Task.id, NOT NULL | 任务 ID |
| task_job_id | Long | FK → TaskJob.id | 任务实例 ID |
| child_id | Long | FK → Child.id, NOT NULL | 儿童 ID |
| status | Enum | NOT NULL | PENDING\|APPROVED\|REJECTED |
| proof | Text | | 完成证明 |
| completed_at | DateTime | NOT NULL | 完成时间 |
| approved_at | DateTime | | 批准时间 |

#### Reward（奖励）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| name | String | NOT NULL(100) | 奖励名称 |
| description | String | (500) | 奖励描述 |
| quantity | Integer | NOT NULL, DEFAULT 999 | 可用数量 |
| points_required | Integer | NOT NULL | 所需积分 |
| image_url | String | (500) | 图片 URL |
| active | Boolean | NOT NULL, DEFAULT true | 是否启用 |

#### RewardRedemption（奖励兑换）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| reward_id | Long | FK → Reward.id, NOT NULL | 奖励 ID |
| child_id | Long | FK → Child.id, NOT NULL | 儿童 ID |
| status | Enum | NOT NULL | REDEEMED\|USED |
| redeemed_at | DateTime | NOT NULL | 兑换时间 |
| used_at | DateTime | | 使用时间 |
| note | String | (500) | 备注 |

#### LotteryTheme（抽奖主题）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| name | String | NOT NULL(100) | 主题名称 |
| description | String | (500) | 主题描述 |
| points_per_draw | Integer | NOT NULL | 每次消耗积分 |
| type | Enum | NOT NULL | FIXED_PROBABILITY\|GUARANTEED\|WEIGHTED_RANDOM |
| active | Boolean | NOT NULL, DEFAULT true | 是否启用 |
| created_by_id | Long | FK → User.id, NOT NULL | 创建者 |
| created_at | DateTime | NOT NULL | 创建时间 |
| updated_at | DateTime | | 更新时间 |

#### LotteryPrize（抽奖奖品）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| lottery_theme_id | Long | FK → LotteryTheme.id, NOT NULL | 主题 ID |
| reward_id | Long | FK → Reward.id | 关联奖励 |
| name | String | NOT NULL(100) | 奖品名称 |
| description | String | (500) | 奖品描述 |
| image_url | String | (500) | 图片 URL |
| weight | Integer | NOT NULL | 权重 |
| probability | Integer | | 概率（百分比） |
| quantity | Integer | NOT NULL, DEFAULT -1 | 数量（-1=无限） |
| redeemed_count | Integer | NOT NULL, DEFAULT 0 | 已兑换数 |
| active | Boolean | NOT NULL, DEFAULT true | 是否启用 |
| created_at | DateTime | NOT NULL | 创建时间 |
| updated_at | DateTime | | 更新时间 |

#### LotteryDraw（抽奖记录）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| lottery_theme_id | Long | FK → LotteryTheme.id, NOT NULL | 主题 ID |
| child_id | Long | FK → Child.id, NOT NULL | 儿童 ID |
| points_cost | Integer | NOT NULL | 消耗积分 |
| result_status | Enum | NOT NULL | JACKPOT\|NO_WIN\|WON |
| draw_at | DateTime | NOT NULL | 抽奖时间 |
| created_at | DateTime | NOT NULL | 创建时间 |

#### LotteryDrawResult（抽奖结果）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| lottery_draw_id | Long | FK → LotteryDraw.id, NOT NULL | 抽奖 ID |
| prize_id | Long | FK → LotteryPrize.id, NOT NULL | 奖品 ID |
| reward_id | Long | FK → Reward.id | 关联奖励 |
| prize_name | String | NOT NULL(100) | 奖品名称 |
| created_at | DateTime | NOT NULL | 创建时间 |

#### PenaltyRule（惩罚规则）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| name | String | NOT NULL(100) | 规则名称 |
| description | String | (500) | 规则描述 |
| points | Integer | NOT NULL | 扣分数 |
| active | Boolean | NOT NULL, DEFAULT true | 是否启用 |
| created_by_id | Long | FK → User.id, NOT NULL | 创建者 |
| created_at | DateTime | NOT NULL | 创建时间 |
| updated_at | DateTime | | 更新时间 |

#### PenaltyRecord（惩罚记录）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| child_id | Long | FK → Child.id, NOT NULL | 儿童 ID |
| penalty_rule_id | Long | FK → PenaltyRule.id, NOT NULL | 规则 ID |
| points | Integer | NOT NULL | 扣分数 |
| note | String | (500) | 备注 |
| applied_by_id | Long | FK → User.id, NOT NULL | 执行者 |
| applied_at | DateTime | NOT NULL | 执行时间 |

#### PointHistory（积分历史）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| child_id | Long | FK → Child.id, NOT NULL | 儿童 ID |
| change_type | Enum | NOT NULL | 变动类型 |
| change_points | Integer | NOT NULL | 变动积分 |
| original_points | Integer | NOT NULL | 原始积分 |
| after_points | Integer | NOT NULL | 变动后积分 |
| description | String | (500) | 描述 |
| reference_id | Long | | 关联记录 ID |
| reference_type | String | (50) | 关联记录类型 |
| changed_by_id | Long | FK → User.id | 操作者 |
| created_at | DateTime | NOT NULL | 创建时间 |

#### Notification（通知）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| title | String | NOT NULL(200) | 通知标题 |
| content | String | NOT NULL(1000) | 通知内容 |
| created_by_id | Long | FK → User.id, NOT NULL | 创建者 |
| target_type | Enum | | ALL\|SPECIFIC_CHILD |
| target_child_id | Long | FK → Child.id | 目标儿童 |
| priority | Enum | | LOW\|NORMAL\|HIGH\|URGENT |
| active | Boolean | NOT NULL, DEFAULT true | 是否启用 |
| created_at | DateTime | NOT NULL | 创建时间 |
| updated_at | DateTime | | 更新时间 |

#### NotificationRead（通知已读）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| notification_id | Long | FK → Notification.id, NOT NULL | 通知 ID |
| child_id | Long | FK → Child.id, NOT NULL | 儿童 ID |
| read_at | DateTime | NOT NULL | 已读时间 |

#### NotificationPreferences（通知偏好）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| user_id | Long | FK → User.id, NOT NULL, UNIQUE | 用户 ID |
| task_reminder | Boolean | NOT NULL, DEFAULT true | 任务提醒 |
| reward_approved | Boolean | NOT NULL, DEFAULT true | 奖励批准 |
| penalty_applied | Boolean | NOT NULL, DEFAULT true | 惩罚通知 |
| system_announcement | Boolean | NOT NULL, DEFAULT true | 系统公告 |
| updated_at | DateTime | | 更新时间 |

#### Feedback（反馈）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| child_id | Long | FK → Child.id, NOT NULL | 儿童 ID |
| category | Enum | NOT NULL | FUNCTIONAL\|NON_FUNCTIONAL\|OTHER |
| title | String | NOT NULL(200) | 反馈标题 |
| description | Text | NOT NULL | 反馈详情 |
| status | Enum | NOT NULL, DEFAULT PENDING | PENDING\|REVIEWED\|ACCEPTED\|REJECTED |
| parent_response | Text | | 家长回复 |
| responded_by_id | Long | FK → User.id | 回复者 |
| responded_at | DateTime | | 回复时间 |
| created_at | DateTime | NOT NULL | 创建时间 |
| updated_at | DateTime | | 更新时间 |

#### Coupon（优惠券）
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, AUTO | 主键 |
| code | String | NOT NULL, UNIQUE(100) | 优惠券代码 |
| points | Integer | NOT NULL | 积分值 |
| enabled | Boolean | NOT NULL, DEFAULT true | 是否启用 |
| comment | String | (500) | 备注 |
| username | String | (50) | 指定用户 |
| timeout_seconds | Integer | NOT NULL | 有效期（秒） |
| used_count | Integer | NOT NULL, DEFAULT 0 | 使用次数 |
| redeemed | Boolean | NOT NULL, DEFAULT false | 是否已兑换 |
| redeemed_by | Long | FK → User.id | 兑换者 |
| redeemed_at | DateTime | | 兑换时间 |
| created_by | Long | FK → User.id | 创建者 |
| created_at | DateTime | NOT NULL | 创建时间 |
| inserted_at | DateTime | | 插入时间 |
| updated_at | DateTime | NOT NULL | 更新时间 |

---

### 5.3 枚举类型

#### UserRole
```java
public enum UserRole {
    PARENT,    // 家长
    CHILD      // 儿童
}
```

#### TaskType
```java
public enum TaskType {
    DAILY_ONCE,    // 每日一次
    MANDATORY,     // 强制任务
    ONE_TIME,      // 一次性任务
    REPEATABLE     // 可重复任务
}
```

#### TaskStatus
```java
public enum TaskStatus {
    DRAFT,      // 草稿
    APPROVED,   // 已批准
    REJECTED    // 已拒绝
}
```

#### TaskDeadlineType
```java
public enum TaskDeadlineType {
    DAILY,          // 每日
    WEEKLY_TIMES    // 每周 N 次
}
```

#### CompletionStatus
```java
public enum CompletionStatus {
    PENDING,    // 待审核
    APPROVED,   // 已批准
    REJECTED    // 已拒绝
}
```

#### RedemptionStatus
```java
public enum RedemptionStatus {
    REDEEMED,   // 已兑换
    USED        // 已使用
}
```

#### PointChangeType
```java
public enum PointChangeType {
    TASK_COMPLETION,     // 任务完成
    REWARD_REDEMPTION,   // 奖励兑换
    PENALTY,             // 惩罚
    LOTTERY_DRAW,        // 抽奖消耗
    LOTTERY_WIN,         // 抽奖中奖
    MANUAL_ADJUSTMENT,   // 手动调整
    INITIAL,             // 初始积分
    OTHER                // 其他
}
```

#### FeedbackCategory
```java
public enum FeedbackCategory {
    FUNCTIONAL,       // 功能建议
    NON_FUNCTIONAL,   // 非功能建议
    OTHER             // 其他
}
```

#### FeedbackStatus
```java
public enum FeedbackStatus {
    PENDING,    // 待审核
    REVIEWED,   // 已审核
    ACCEPTED,   // 已采纳
    REJECTED    // 已拒绝
}
```

#### LotteryTypeEnum
```java
public enum LotteryTypeEnum {
    FIXED_PROBABILITY,   // 固定概率
    GUARANTEED,          // 必中
    WEIGHTED_RANDOM      // 权重随机
}
```

#### DrawResultStatus
```java
public enum DrawResultStatus {
    JACKPOT,    // 大奖
    WON,        // 中奖
    NO_WIN      // 未中奖
}
```

#### NotificationPriority
```java
public enum NotificationPriority {
    LOW,       // 低
    NORMAL,    // 普通
    HIGH,      // 高
    URGENT     // 紧急
}
```

#### NotificationTargetType
```java
public enum NotificationTargetType {
    ALL,            // 所有人
    SPECIFIC_CHILD  // 指定儿童
}
```

#### JobStatus
```java
public enum JobStatus {
    ASSIGNED,     // 已分配
    IN_PROGRESS,  // 进行中
    COMPLETED,    // 已完成
    CANCELLED     // 已取消
}
```

#### PenaltyNotificationStatus
```java
public enum PenaltyNotificationStatus {
    PENDING,    // 待处理
    APPLIED,    // 已执行
    DISMISSED,  // 已撤销
    EXPIRED     // 已过期
}
```

---

## 6. 业务流程

### 6.1 任务完成流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   家长创建   │────►│  系统分配   │────►│  儿童完成   │
│    任务     │     │  任务实例   │     │  并提交    │
└─────────────┘     └─────────────┘     └─────────────┘
                                              │
                                              ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  积分发放   │◄────│  家长审核   │◄────│  状态更新   │
│  (批准后)   │     │  (批准/拒绝)│     │  为 PENDING │
└─────────────┘     └─────────────┘     └─────────────┘
```

**详细步骤**
1. 家长创建任务（Task）
2. 系统自动或手动创建任务实例（TaskJob）分配给儿童
3. 儿童完成任务并提交（TaskCompletion, status=PENDING）
4. 家长收到审核通知
5. 家长审核：
   - 批准：TaskCompletion.status=APPROVED，增加积分，创建 PointHistory
   - 拒绝：TaskCompletion.status=REJECTED，TaskJob.status=ASSIGNED（可重新提交）

---

### 6.2 奖励兑换流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   儿童浏览   │────►│  选择奖励   │────►│  提交兑换   │
│   奖励列表   │     │  并确认    │     │   请求     │
└─────────────┘     └─────────────┘     └─────────────┘
                                              │
                                              ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  创建兑换   │◄────│  创建积分   │◄────│  验证积分   │
│   记录     │     │  历史记录   │     │  并扣除    │
└─────────────┘     └─────────────┘     └─────────────┘
```

**详细步骤**
1. 儿童浏览可用奖励列表
2. 儿童选择奖励并确认兑换
3. 系统验证：
   - 奖励存在且 active=true
   - 奖励数量充足
   - 儿童积分充足
4. 扣除儿童积分
5. 减少奖励数量
6. 创建 RewardRedemption 记录
7. 创建 PointHistory 记录

---

### 6.3 抽奖流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   儿童选择   │────►│  系统验证   │────►│  执行抽奖   │
│   抽奖主题   │     │  积分和奖品 │     │   算法     │
└─────────────┘     └─────────────┘     └─────────────┘
                                              │
                                              ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  发放奖品   │◄────│  创建抽奖   │◄────│  确定中奖   │
│  (如有关联) │     │  记录      │     │   奖品     │
└─────────────┘     └─────────────┘     └─────────────┘
```

**详细步骤**
1. 儿童选择抽奖主题
2. 系统验证：
   - 主题存在且 active=true
   - 儿童积分充足
   - 奖品池非空
3. 扣除积分
4. 执行权重随机算法选择奖品
5. 创建 LotteryDraw 记录
6. 创建 LotteryDrawResult 记录
7. 如果奖品关联 Reward，创建 RewardRedemption
8. 创建 PointHistory 记录（抽奖消耗）

---

### 6.4 惩罚执行流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   家长选择   │────►│  填写备注   │────►│  提交惩罚   │
│   惩罚规则   │     │  (可选)    │     │   请求     │
└─────────────┘     └─────────────┘     └─────────────┘
                                              │
                                              ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  创建通知   │◄────│  创建惩罚   │◄────│  扣除积分   │
│  (可选)    │     │   记录     │     │            │
└─────────────┘     └─────────────┘     └─────────────┘
```

**详细步骤**
1. 家长选择惩罚规则
2. 家长选择目标儿童
3. 家长填写备注（可选）
4. 系统验证规则存在且 active=true
5. 扣除儿童积分
6. 创建 PenaltyRecord 记录
7. 创建 PointHistory 记录
8. 可选：创建 Notification 通知儿童

---

## 7. 状态机定义

### 7.1 任务状态机

```
                    ┌─────────────┐
                    │    DRAFT    │
                    └──────┬──────┘
                           │ 家长批准
                           ▼
                    ┌─────────────┐
         ┌─────────│   APPROVED  │─────────┐
         │         └──────┬──────┘         │
         │                │ 家长停用       │ 家长拒绝
         │                ▼                │
         │         ┌─────────────┐         │
         │         │   INACTIVE  │         │
         │         └─────────────┘         │
         │                                 │
         └─────────────────────────────────┘
```

### 7.2 任务实例状态机

```
                    ┌─────────────┐
                    │   ASSIGNED  │
                    └──────┬──────┘
                           │ 儿童开始
                           ▼
                    ┌─────────────┐
                    │ IN_PROGRESS │
                    └──────┬──────┘
                           │ 儿童提交
                           ▼
                    ┌─────────────┐
         ┌─────────│  COMPLETED  │─────────┐
         │         └──────┬──────┘         │
         │ 重新分配       │ 家长批准       │ 家长拒绝
         │                ▼                │
         │         ┌─────────────┐         │
         │         │   AWAITING  │         │
         │         │   APPROVAL  │         │
         │         └─────────────┘         │
         │                                 │
         └─────────────────────────────────┘
```

### 7.3 任务完成状态机

```
                    ┌─────────────┐
                    │   PENDING   │
                    └──────┬──────┘
                           │
              ┌────────────┴────────────┐
              │                         │
              ▼                         ▼
       ┌─────────────┐          ┌─────────────┐
       │   APPROVED  │          │   REJECTED  │
       └─────────────┘          └─────────────┘
              │                         │
              │                         │ 允许重新提交
              ▼                         ▼
       ┌─────────────┐          ┌─────────────┐
       │   FINAL     │          │  RESUBMIT   │
       └─────────────┘          └─────────────┘
```

### 7.4 奖励兑换状态机

```
                    ┌─────────────┐
                    │  REDEEMED   │
                    └──────┬──────┘
                           │ 家长标记使用
                           ▼
                    ┌─────────────┐
                    │    USED     │
                    └─────────────┘
```

### 7.5 反馈状态机

```
                    ┌─────────────┐
                    │   PENDING   │
                    └──────┬──────┘
                           │ 家长审核
                           ▼
                    ┌─────────────┐
         ┌─────────│   REVIEWED  │─────────┐
         │         └──────┬──────┘         │
         │                │                │
         ▼                ▼                ▼
  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
  │   ACCEPTED  │  │   REJECTED  │  │   PENDING   │
  └─────────────┘  └─────────────┘  └─────────────┘
       │                │
       │                │
       ▼                ▼
  ┌─────────────┐  ┌─────────────┐
  │   FINAL     │  │   FINAL     │
  └─────────────┘  └─────────────┘
```

---

## 8. 验证规则

### 8.1 字段级验证

#### 通用规则
| 字段类型 | 规则 | 错误消息 |
|----------|------|----------|
| String (required) | 不能为空 | "{field} 不能为空" |
| String (1-100) | 长度 1-100 | "{field} 长度必须在 1-100 之间" |
| String (0-500) | 长度 0-500 | "{field} 长度不能超过 500" |
| Integer (positive) | > 0 | "{field} 必须大于 0" |
| Integer (1-999) | 1-999 | "{field} 必须在 1-999 之间" |
| DateTime (future) | 不能是过去 | "{field} 不能是过去时间" |

#### 特定实体验证

**Task**
```java
@NotNull(message = "标题不能为空")
@Size(min = 1, max = 100, message = "标题长度必须在 1-100 之间")
private String title;

@NotNull(message = "积分不能为空")
@Min(value = 1, message = "积分必须大于 0")
@Max(value = 999, message = "积分不能超过 999")
private Integer points;

@NotNull(message = "任务类型不能为空")
private TaskType type;

// MANDATORY 类型必须设置 deadline 字段
@AssertTrue(message = "强制任务必须设置截止时间类型")
private boolean isValidDeadline() {
    if (type == TaskType.MANDATORY) {
        return deadlineType != null && deadlineValue != null;
    }
    return true;
}
```

**Reward**
```java
@NotNull(message = "奖励名称不能为空")
@Size(min = 1, max = 100, message = "奖励名称长度必须在 1-100 之间")
private String name;

@NotNull(message = "所需积分不能为空")
@Min(value = 1, message = "所需积分必须大于 0")
@Max(value = 999, message = "所需积分不能超过 999")
private Integer pointsRequired;

@Min(value = 1, message = "数量必须大于 0")
@Max(value = 999, message = "数量不能超过 999")
private Integer quantity;
```

**Child**
```java
@NotNull(message = "积分不能为空")
@Min(value = 0, message = "积分不能为负数")
private Integer points;
```

---

### 8.2 业务规则验证

#### 任务完成验证
```java
// DAILY_ONCE 类型：每天只能完成一次
public void validateDailyOnceCompletion(Long taskId, Long childId) {
    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    LocalDateTime todayEnd = todayStart.plusDays(1);
    
    long count = taskCompletionRepository.countByTaskIdAndChildIdAndCompletedAtBetween(
        taskId, childId, todayStart, todayEnd
    );
    
    if (count > 0) {
        throw new BusinessException("TASK_ALREADY_COMPLETED_TODAY", "今天已完成此任务");
    }
}

// MANDATORY 类型：检查完成次数
public void validateMandatoryTaskCompletion(Task task, Long childId) {
    if (task.getDeadlineType() == TaskDeadlineType.WEEKLY_TIMES) {
        LocalDateTime weekStart = getWeekStart();
        LocalDateTime weekEnd = weekStart.plusWeeks(1);
        
        long count = taskCompletionRepository.countByTaskIdAndChildIdAndCompletedAtBetween(
            task.getId(), childId, weekStart, weekEnd
        );
        
        if (count >= task.getDeadlineValue()) {
            throw new BusinessException("WEEKLY_LIMIT_REACHED", "本周已完成最大次数");
        }
    }
}
```

#### 积分验证
```java
// 兑换奖励：检查积分充足
public void validatePointsSufficient(Child child, int requiredPoints) {
    if (child.getPoints() < requiredPoints) {
        throw new BusinessException(
            "INSUFFICIENT_POINTS", 
            "积分不足，当前积分：" + child.getPoints() + ", 需要：" + requiredPoints
        );
    }
}

// 惩罚执行：允许负数积分，无需验证
```

#### 奖励库存验证
```java
// 检查奖励库存
public void validateRewardQuantity(Reward reward) {
    if (!reward.isActive()) {
        throw new BusinessException("REWARD_INACTIVE", "该奖励已停用");
    }
    
    // quantity = 999 表示无限
    if (reward.getQuantity() != 999 && reward.getQuantity() <= 0) {
        throw new BusinessException("REWARD_OUT_OF_STOCK", "奖励库存不足");
    }
}
```

---

### 8.3 唯一性约束

| 实体 | 字段 | 范围 | 错误消息 |
|------|------|------|----------|
| User | username | 全局 | "用户名已存在" |
| Child | username | 全局 | "用户名已存在" |
| Coupon | code | 全局 | "优惠券代码已存在" |
| NotificationRead | notification_id + child_id | 组合唯一 | "通知已标记为已读" |

---

## 9. 错误处理

### 9.1 错误码规范

**格式**: `MODULE_ERROR_TYPE`

| 模块 | 前缀 | 示例 |
|------|------|------|
| 认证 | AUTH_ | AUTH_LOGIN_FAILED |
| 任务 | TASK_ | TASK_NOT_FOUND |
| 奖励 | REWARD_ | REWARD_INSUFFICIENT_POINTS |
| 抽奖 | LOTTERY_ | LOTTERY_THEME_NOT_FOUND |
| 惩罚 | PENALTY_ | PENALTY_RULE_NOT_FOUND |
| 积分 | POINTS_ | POINTS_INSUFFICIENT |
| 反馈 | FEEDBACK_ | FEEDBACK_NOT_FOUND |
| 通知 | NOTIFICATION_ | NOTIFICATION_NOT_FOUND |
| 通用 | COMMON_ | COMMON_VALIDATION_ERROR |

---

### 9.2 错误码列表

#### 认证错误
| 错误码 | HTTP 状态 | 消息 |
|--------|----------|------|
| AUTH_LOGIN_FAILED | 401 | 用户名或密码错误 |
| AUTH_USER_NOT_FOUND | 404 | 用户不存在 |
| AUTH_USER_DISABLED | 403 | 用户已被禁用 |
| AUTH_UNAUTHORIZED | 401 | 未登录 |
| AUTH_FORBIDDEN | 403 | 无权访问 |

#### 任务错误
| 错误码 | HTTP 状态 | 消息 |
|--------|----------|------|
| TASK_NOT_FOUND | 404 | 任务不存在 |
| TASK_INACTIVE | 400 | 任务已停用 |
| TASK_ALREADY_COMPLETED_TODAY | 400 | 今天已完成此任务 |
| TASK_NOT_ASSIGNED | 400 | 任务未分配给你 |
| WEEKLY_LIMIT_REACHED | 400 | 本周已完成最大次数 |
| TASK_COMPLETION_NOT_FOUND | 404 | 完成记录不存在 |
| TASK_COMPLETION_ALREADY_REVIEWED | 400 | 该完成记录已审核 |

#### 奖励错误
| 错误码 | HTTP 状态 | 消息 |
|--------|----------|------|
| REWARD_NOT_FOUND | 404 | 奖励不存在 |
| REWARD_INACTIVE | 400 | 该奖励已停用 |
| REWARD_OUT_OF_STOCK | 400 | 奖励库存不足 |
| REWARD_INSUFFICIENT_POINTS | 400 | 积分不足 |
| REWARD_REDEMPTION_NOT_FOUND | 404 | 兑换记录不存在 |

#### 抽奖错误
| 错误码 | HTTP 状态 | 消息 |
|--------|----------|------|
| LOTTERY_THEME_NOT_FOUND | 404 | 抽奖主题不存在 |
| LOTTERY_THEME_INACTIVE | 400 | 该抽奖已停用 |
| LOTTERY_INSUFFICIENT_POINTS | 400 | 积分不足 |
| LOTTERY_EMPTY_PRIZE_POOL | 400 | 奖品池为空 |
| LOTTERY_PRIZE_NOT_FOUND | 404 | 奖品不存在 |

#### 惩罚错误
| 错误码 | HTTP 状态 | 消息 |
|--------|----------|------|
| PENALTY_RULE_NOT_FOUND | 404 | 惩罚规则不存在 |
| PENALTY_RULE_INACTIVE | 400 | 该规则已停用 |
| PENALTY_RECORD_NOT_FOUND | 404 | 惩罚记录不存在 |

#### 积分错误
| 错误码 | HTTP 状态 | 消息 |
|--------|----------|------|
| POINTS_INSUFFICIENT | 400 | 积分不足 |
| POINTS_HISTORY_NOT_FOUND | 404 | 积分记录不存在 |

#### 反馈错误
| 错误码 | HTTP 状态 | 消息 |
|--------|----------|------|
| FEEDBACK_NOT_FOUND | 404 | 反馈不存在 |
| FEEDBACK_ALREADY_REVIEWED | 400 | 该反馈已审核 |

#### 通知错误
| 错误码 | HTTP 状态 | 消息 |
|--------|----------|------|
| NOTIFICATION_NOT_FOUND | 404 | 通知不存在 |
| NOTIFICATION_ALREADY_READ | 400 | 通知已标记为已读 |

#### 通用错误
| 错误码 | HTTP 状态 | 消息 |
|--------|----------|------|
| COMMON_VALIDATION_ERROR | 400 | 参数验证失败 |
| COMMON_NOT_FOUND | 404 | 资源不存在 |
| COMMON_INTERNAL_ERROR | 500 | 系统内部错误 |
| COMMON_METHOD_NOT_ALLOWED | 405 | 方法不允许 |

---

### 9.3 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ApiResponse<Void> response = ApiResponse.error(
            ex.getErrorCode(),
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
        ResourceNotFoundException ex
    ) {
        ApiResponse<Void> response = ApiResponse.error(
            "COMMON_NOT_FOUND",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
        MethodArgumentNotValidException ex
    ) {
        List<Map<String, String>> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> Map.of(
                "field", error.getField(),
                "message", error.getDefaultMessage()
            ))
            .collect(Collectors.toList());

        ApiResponse<Void> response = ApiResponse.error(
            "COMMON_VALIDATION_ERROR",
            "参数验证失败",
            details
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ApiResponse<Void> response = ApiResponse.error(
            "COMMON_INTERNAL_ERROR",
            "系统内部错误：" + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

---

## 10. 非功能需求

### 10.1 性能要求

| 指标 | 目标值 | 说明 |
|------|--------|------|
| API 响应时间 (P95) | < 500ms | 95% 的请求在 500ms 内响应 |
| API 响应时间 (P99) | < 1000ms | 99% 的请求在 1s 内响应 |
| 页面加载时间 | < 2s | 首屏加载时间 |
| 并发用户数 | 50 | 支持 50 个并发用户 |
| 数据库查询时间 | < 100ms | 简单查询 |
| 数据库查询时间 (复杂) | < 500ms | 复杂关联查询 |

### 10.2 可用性要求

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 系统可用性 | 99.5% | 月度可用性 |
| 计划维护窗口 | 每周日 02:00-04:00 | 提前通知 |
| 故障恢复时间 | < 30 分钟 | 严重故障 |
| 数据备份频率 | 每日 | 自动备份 |
| 数据保留期 | 30 天 | 备份文件 |

### 10.3 安全要求

| 要求 | 实现方式 |
|------|----------|
| 密码存储 | BCrypt 加密，cost=10 |
| 会话管理 | Session-based, 30 分钟超时 |
| CSRF 保护 | 生产环境启用 |
| SQL 注入防护 | JPA 参数化查询 |
| XSS 防护 | Thymeleaf 自动转义 |
| 输入验证 | 所有输入严格校验 |
| 日志审计 | 关键操作记录日志 |
| 数据隔离 | 基于角色的数据访问控制 |

### 10.4 兼容性要求

| 平台 | 版本要求 |
|------|----------|
| Java | OpenJDK 21+ |
| MySQL | 8.0+ |
| Chrome | 最新 2 个版本 |
| Safari | 最新 2 个版本 |
| Firefox | 最新 2 个版本 |
| Edge | 最新 2 个版本 |
| 移动端 | iOS 14+, Android 10+ |

### 10.5 国际化要求

| 要求 | 说明 |
|------|------|
| 默认语言 | 中文 (zh-CN) |
| 时区 | Asia/Shanghai |
| 日期格式 | yyyy-MM-dd HH:mm:ss |
| 数字格式 | 不使用千分位 |

---

## 11. 测试场景

### 11.1 单元测试场景

#### 任务服务测试
```java
@Test
void createTask_shouldCreateTaskSuccessfully() {
    // Given
    CreateTaskRequest request = new CreateTaskRequest();
    request.setTitle("测试任务");
    request.setPoints(10);
    request.setType(TaskType.DAILY_ONCE);
    
    // When
    TaskDTO result = taskService.createTask(request, 1L);
    
    // Then
    assertThat(result.getId()).isNotNull();
    assertThat(result.getTitle()).isEqualTo("测试任务");
    assertThat(result.getStatus()).isEqualTo(TaskStatus.APPROVED);
}

@Test
void createTask_shouldFailWhenTitleIsEmpty() {
    // Given
    CreateTaskRequest request = new CreateTaskRequest();
    request.setTitle("");
    
    // When & Then
    assertThatThrownBy(() -> taskService.createTask(request, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessage("标题不能为空");
}

@Test
void completeTask_shouldFailWhenAlreadyCompletedToday() {
    // Given
    Task task = createTask(TaskType.DAILY_ONCE);
    Child child = createChild();
    completeTask(task, child); // 已完成一次
    
    // When & Then
    assertThatThrownBy(() -> taskService.completeTask(task.getId(), child.getId()))
        .isInstanceOf(BusinessException.class)
        .hasMessage("今天已完成此任务");
}
```

#### 奖励服务测试
```java
@Test
void redeemReward_shouldDeductPointsAndCreateRedemption() {
    // Given
    Reward reward = createReward(50);
    Child child = createChild(100); // 100 积分
    
    // When
    RewardRedemptionDTO result = rewardService.redeemReward(reward.getId(), child.getId());
    
    // Then
    assertThat(result.getPointsUsed()).isEqualTo(50);
    assertThat(childRepository.findById(child.getId()).get().getPoints()).isEqualTo(50);
    assertThat(rewardRepository.findById(reward.getId()).get().getQuantity()).isEqualTo(998);
}

@Test
void redeemReward_shouldFailWhenInsufficientPoints() {
    // Given
    Reward reward = createReward(100);
    Child child = createChild(50); // 50 积分
    
    // When & Then
    assertThatThrownBy(() -> rewardService.redeemReward(reward.getId(), child.getId()))
        .isInstanceOf(BusinessException.class)
        .hasMessage("积分不足");
}
```

#### 抽奖服务测试
```java
@Test
void draw_shouldSelectPrizeBasedOnWeight() {
    // Given
    LotteryTheme theme = createTheme(LotteryTypeEnum.WEIGHTED_RANDOM);
    LotteryPrize prize1 = createPrize(theme, 30); // 权重 30
    LotteryPrize prize2 = createPrize(theme, 70); // 权重 70
    Child child = createChild(100);
    
    // When - 执行 1000 次抽奖
    Map<Long, Integer> results = new HashMap<>();
    for (int i = 0; i < 1000; i++) {
        LotteryDrawDTO result = lotteryService.draw(theme.getId(), child.getId());
        results.merge(result.getPrizeId(), 1, Integer::sum);
    }
    
    // Then - 奖品 2 应该约占 70%
    double prize2Ratio = results.get(prize2.getId()) / 1000.0;
    assertThat(prize2Ratio).isBetween(0.65, 0.75);
}
```

---

### 11.2 集成测试场景

#### 任务完成流程测试
```java
@Test
void taskCompletionFlow_shouldAwardPointsAfterApproval() {
    // Given
    User parent = createParent();
    Child child = createChild(parent, 0); // 0 积分
    Task task = createTask(parent, 5); // 5 积分
    TaskJob taskJob = assignTask(task, child);
    
    // When - 儿童提交完成
    TaskCompletionDTO completion = taskService.completeTask(task.getId(), child.getId());
    assertThat(completion.getStatus()).isEqualTo(CompletionStatus.PENDING);
    
    // 家长批准
    TaskCompletionDTO approved = taskService.approveCompletion(completion.getId());
    assertThat(approved.getStatus()).isEqualTo(CompletionStatus.APPROVED);
    
    // Then
    Child updatedChild = childRepository.findById(child.getId()).get();
    assertThat(updatedChild.getPoints()).isEqualTo(5);
    
    PointHistory history = pointHistoryRepository.findAll().get(0);
    assertThat(history.getChangeType()).isEqualTo(PointChangeType.TASK_COMPLETION);
    assertThat(history.getChangePoints()).isEqualTo(5);
    assertThat(history.getAfterPoints()).isEqualTo(5);
}
```

#### 奖励兑换流程测试
```java
@Test
void rewardRedemptionFlow_shouldDeductPointsAndCreateHistory() {
    // Given
    Child child = createChild(100); // 100 积分
    Reward reward = createReward(50); // 50 积分
    
    // When
    RewardRedemptionDTO redemption = rewardService.redeemReward(reward.getId(), child.getId());
    
    // Then
    Child updatedChild = childRepository.findById(child.getId()).get();
    assertThat(updatedChild.getPoints()).isEqualTo(50);
    
    PointHistory history = pointHistoryRepository.findAll().get(0);
    assertThat(history.getChangeType()).isEqualTo(PointChangeType.REWARD_REDEMPTION);
    assertThat(history.getChangePoints()).isEqualTo(-50);
    assertThat(history.getAfterPoints()).isEqualTo(50);
}
```

---

### 11.3 UI 测试场景（Selenium）

#### 登录流程测试
```java
@Test
void loginFlow_shouldLoginSuccessfully() {
    // Given
    driver.get(baseUrl + "/login");
    
    // When
    driver.findElement(By.id("username")).sendKeys("parent");
    driver.findElement(By.id("password")).sendKeys("parent123");
    driver.findElement(By.id("loginBtn")).click();
    
    // Then
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    WebElement dashboard = wait.until(
        ExpectedConditions.visibilityOfElementLocated(By.id("dashboard"))
    );
    assertThat(dashboard.isDisplayed()).isTrue();
}
```

#### 任务创建流程测试
```java
@Test
void taskCreationFlow_shouldCreateTaskSuccessfully() {
    // Given
    login("parent", "parent123");
    driver.get(baseUrl + "/parent/tasks");
    
    // When
    driver.findElement(By.id("createTaskBtn")).click();
    driver.findElement(By.id("title")).sendKeys("新任务");
    driver.findElement(By.id("points")).sendKeys("10");
    driver.findElement(By.id("type")).sendKeys("DAILY_ONCE");
    driver.findElement(By.id("saveBtn")).click();
    
    // Then
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    WebElement successMessage = wait.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(text(), '任务创建成功')]")
        )
    );
    assertThat(successMessage.isDisplayed()).isTrue();
}
```

---

### 11.4 性能测试场景

#### 并发任务完成测试
```
场景：50 个并发用户同时提交任务完成
预期：所有请求在 5s 内完成，无数据不一致

配置：
- 并发用户数：50
- 持续时间：60s
- 目标 API：POST /api/v1/tasks/{id}/complete

验收标准：
- P95 响应时间 < 500ms
- 错误率 < 1%
- 积分计算准确（无重复加分）
```

#### 大数据量查询测试
```
场景：查询 10000 条积分历史记录
预期：分页查询响应时间 < 1s

配置：
- 数据量：10000 条 PointHistory 记录
- 查询：GET /api/v1/points/history?page=0&size=20

验收标准：
- 响应时间 < 500ms
- 内存使用 < 100MB
```

---

### 11.5 验收测试清单

#### 核心功能验收
- [ ] 家长可以创建、编辑、删除任务
- [ ] 儿童可以查看分配给自己的任务
- [ ] 儿童可以提交任务完成
- [ ] 家长可以批准或拒绝任务完成
- [ ] 批准后积分正确发放
- [ ] 拒绝后儿童可以重新提交
- [ ] 家长可以创建、编辑、删除奖励
- [ ] 儿童可以兑换奖励
- [ ] 兑换后积分正确扣除
- [ ] 家长可以创建抽奖主题和奖品
- [ ] 儿童可以参与抽奖
- [ ] 抽奖算法符合权重分布
- [ ] 家长可以创建惩罚规则
- [ ] 家长可以执行惩罚
- [ ] 惩罚后积分正确扣除
- [ ] 用户可以提交反馈
- [ ] 家长可以回复反馈
- [ ] 家长可以创建通知
- [ ] 儿童可以查看和标记通知已读
- [ ] 积分历史记录准确

#### 边界条件验收
- [ ] 积分可以为 0
- [ ] 积分可以为负数（惩罚后）
- [ ] 奖励数量可以为 999（无限）
- [ ] 任务可以停用后重新启用
- [ ] DAILY_ONCE 任务每天只能完成一次
- [ ] MANDATORY 任务未完成时触发惩罚
- [ ] 抽奖奖品数量为 0 时不能抽中
- [ ] 用户登录失败 5 次后锁定（可选）

#### 安全验收
- [ ] 未登录用户不能访问 API
- [ ] 儿童不能访问其他儿童的数据
- [ ] 儿童不能执行家长操作
- [ ] SQL 注入攻击被阻止
- [ ] XSS 攻击被阻止
- [ ] CSRF 保护生效（生产环境）

---

## 附录

### A. 版本历史

| 版本 | 日期 | 变更说明 | 作者 |
|------|------|----------|------|
| 1.4.2 | 2026-04-17 | 修复版本号自动更新问题 | System |
| 1.4.1 | 2026-04-17 | 时区修复（Asia/Shanghai） | System |
| 1.4.0 | 2026-04-10 | 禁止直接修改数据库，强制使用 Liquibase | System |
| 1.3.0 | 2026-04-07 | 简化 Changelog 结构 | System |
| 1.0.0 | 2026-04-04 | 初始版本 | System |

### B. 参考文档

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Security 官方文档](https://spring.io/projects/spring-security)
- [Spring Data JPA 官方文档](https://spring.io/projects/spring-data-jpa)
- [Thymeleaf 官方文档](https://www.thymeleaf.org/)
- [OpenAPI 规范](https://swagger.io/specification/)

### C. 联系方式

- **项目仓库**: https://github.com/terrycong/creditapp
- **镜像仓库**: https://ghcr.io/terrycong/creditapp
- **文档维护**: 随代码变更同步更新

---

**文档状态**: ✅ 可用于 Spec-Driven Development  
**最后审查**: 2026-04-17  
**下次审查**: 每次重大功能变更前
