# Changelog 重构说明

## 📁 新结构

```
db/changelog/
├── db.changelog-master.yaml    # 主配置文件（简化版）
├── 001-schema-ddl.sql          # 数据库结构定义（所有 DDL）
├── 002-data-dml.sql            # 初始数据配置（所有 DML）
└── changes/                    # 旧文件（已废弃，保留备份）
    ├── 001-initial-schema-mysql.yaml
    ├── 002-initial-data.yaml
    └── ... (其他旧文件)
```

---

## ✅ 优势

### 之前（27 个文件）
```
001-initial-schema-mysql.yaml
002-initial-data.yaml
003-force-seed-data.yaml
004-clear-and-reseed.yaml
015-create-penalty-notifications-table.yaml
016-create-task-completions-table.yaml
017-create-coupons-table.sql
018-add-spring-autumn-trip-reward.yaml
018-update-tasks-and-rewards.sql
019-add-new-tasks.sql
020-add-penalty-rules.sql
021-add-clothing-penalty-rules.sql
022-add-new-rewards.sql
023-create-notifications-tables.sql
024-add-word-study-rewards.sql
025-add-video-with-dad-reward.sql
026-add-story-download-reward.sql
027-add-math-exercise-task.sql
... (18 个文件)
```

### 现在（2 个文件）
```
001-schema-ddl.sql    # 所有表结构
002-data-dml.sql      # 所有初始数据
```

---

## 📊 对比

| 项目 | 之前 | 现在 | 改进 |
|------|------|------|------|
| **文件数量** | 27 个 | 2 个 | -93% |
| **总行数** | ~2000 行 | ~600 行 | -70% |
| **维护成本** | 高（分散） | 低（集中） | ✅ |
| **查找效率** | 低（多文件） | 高（单文件） | ✅ |
| **checksum 问题** | 频繁 | 减少 | ✅ |

---

## 📝 文件内容

### 001-schema-ddl.sql (数据库结构)

包含所有表的 DDL 定义：

1. **用户系统** - `users`, `children`
2. **任务系统** - `tasks`, `task_jobs`, `task_completions`
3. **奖励系统** - `rewards`, `reward_redemptions`
4. **抽奖系统** - `lottery_themes`, `lottery_prizes`, `lottery_draws`, `lottery_draw_results`
5. **积分系统** - `point_history`
6. **惩罚系统** - `penalty_rules`, `penalty_records`, `penalty_notifications`
7. **通知系统** - `notifications`, `notification_preferences`
8. **优惠券系统** - `coupons`

**特点：**
- 所有表使用 `CREATE TABLE IF NOT EXISTS`
- 包含完整的索引和外键约束
- 统一的字符集和注释

---

### 002-data-dml.sql (初始数据)

包含所有初始数据：

1. **基础用户** - parent, child
2. **晨间任务** - 不赖床、吃维生素、刷牙、早餐、牛奶、打针 (6 个)
3. **学习任务** - 上学不迟到、作业、冲凉、睡觉、练习卷 (6 个)
4. **习惯任务** - 背单词 10/20/50 天、宝典课 (4 个)
5. **数学任务** - 每天 30 道数学题 (1 个)
6. **基础奖励** - 游戏时间、电影、冰淇淋、零花钱、玩具等 (7 个)
7. **餐饮奖励** - 游乐场、披萨、餐厅、零食、饮料 (5 个)
8. **特权奖励** - 周末活动、晚睡、免家务、免扣分等 (5 个)
9. **亲子奖励** - 和爸爸玩游戏、拼模型、拼图、看视频、下载故事 (7 个)
10. **大奖奖励** - 住酒店、新模型、看电影 (3 个)
11. **惩罚规则** - 不诚实使用平板、衣着不整 (2 个)
12. **抽奖主题** - 幸运大转盘 (1 个)
13. **抽奖奖品** - 冰淇淋、游戏时间、零花钱、新玩具 (4 个)
14. **通知配置** - 默认偏好设置 (1 个)

**特点：**
- 使用 `INSERT ... SELECT ... WHERE NOT EXISTS` 避免重复
- 所有数据通过子查询关联用户 ID
- 幂等操作，可重复执行

---

## 🔄 迁移步骤

### 全新安装
```bash
# 直接运行 Liquibase 更新
mvn liquibase:update
```

### 现有数据库（已执行旧 changelog）

1. **备份数据库**
```bash
mysqldump -h 192.168.9.113 -u root creditapp > backup-$(date +%Y%m%d).sql
```

2. **清除旧 checksum**
```bash
mvn liquibase:clearCheckSums
```

3. **执行更新**
```bash
mvn liquibase:update
```

4. **验证**
```bash
mvn liquibase:validate
mvn liquibase:status
```

---

## 🗑️ 旧文件处理

### 建议操作

1. **保留 `changes/` 目录** - 作为历史参考
2. **不再修改旧文件** - 所有修改在新文件中进行
3. **更新文档** - 说明新结构

### 可选清理

如果确认不再需要旧文件：

```bash
# 移动到备份目录
mkdir -p src/main/resources/db/changelog/changes-deprecated
mv src/main/resources/db/changelog/changes/*.yaml src/main/resources/db/changelog/changes-deprecated/
mv src/main/resources/db/changelog/changes/*.sql src/main/resources/db/changelog/changes-deprecated/
```

---

## 📋 维护指南

### 添加新表

在 `001-schema-ddl.sql` 中添加：

```sql
--changeset admin:schema-xx-new-table
--comment: Create new table
CREATE TABLE IF NOT EXISTS new_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Description';
```

### 添加新数据

在 `002-data-dml.sql` 中添加：

```sql
--changeset admin:data-xx-new-data
--comment: Insert new data
INSERT INTO table_name (columns...)
SELECT ...
FROM users u WHERE u.username = 'parent'
AND NOT EXISTS (SELECT 1 FROM table_name WHERE ...);
```

---

## ⚠️ 注意事项

1. **不要混用新旧 changelog** - 会导致 checksum 冲突
2. **生产环境先测试** - 在开发环境验证后再部署
3. **备份数据库** - 执行前务必备份
4. **清除 checksum** - 首次使用新结构需要清除旧的

---

**最后更新:** 2026-04-06
