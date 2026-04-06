# creditapp 数据库迁移指南

本文档说明如何安全地执行数据库迁移。

---

## 🚀 快速开始

### 使用自动化脚本（推荐）

```bash
bash /vol1/@apphome/trim.openclaw/data/workspace/scripts/update-creditapp-db.sh
```

脚本会自动执行以下步骤：
1. ✅ 验证 changelog
2. ✅ 显示待执行的变更
3. ✅ 执行更新
4. ✅ 验证结果

---

## 📋 手动执行流程

### Step 1: 验证 Changelog

```bash
cd /vol1/@apphome/trim.openclaw/data/workspace/creditapp
mvn liquibase:validate
```

**预期输出:**
```
[INFO] No validation errors found.
[INFO] BUILD SUCCESS
```

**如果失败:**
```
[ERROR] Validation Failed:
  1 changesets check sum
       db/changelog/changes/024-add-word-study-rewards.sql::024::admin 
       was: 9:d5096a07... but is now: 9:88d9810...
```

**解决方案:**
- **开发环境:** `mvn liquibase:clearCheckSums`
- **生产环境:** 不要修改已执行的 changeset，创建新的

---

### Step 2: 检查状态

```bash
mvn liquibase:status
```

**预期输出:**
```
root@192.168.9.113@jdbc:mysql:... is up to date
```

或者显示待执行的变更集列表。

---

### Step 3: 执行更新

```bash
mvn liquibase:update
```

**预期输出:**
```
UPDATE SUMMARY
Run:                          2
Previously run:              28
Total change sets:           30

[INFO] BUILD SUCCESS
```

---

### Step 4: 验证结果

```bash
mvn liquibase:validate
mvn liquibase:status
```

---

## 🔧 常见问题

### Q1: Checksum 不匹配

**症状:**
```
Validation Failed: changeset checksum mismatch
```

**原因:**
- Changelog 文件被修改
- 文件编码变化（UTF-8 → UTF-8 with BOM）
- 换行符变化（LF → CRLF）

**解决:**
```bash
# 开发环境
mvn liquibase:clearCheckSums

# 或者在数据库中清除特定 changeset
UPDATE DATABASECHANGELOG 
SET md5sum = NULL 
WHERE filename = 'db/changelog/changes/xxx.sql';
```

---

### Q2: 表已存在

**症状:**
```
Table 'xxx' already exists [Failed SQL: (1050) CREATE TABLE ...]
```

**原因:**
- 重复的 changelog（表已在其他文件中创建）
- 手动创建了表但 changelog 未标记

**解决:**
```sql
-- 标记为已执行
SET @max_order = (SELECT MAX(orderexecuted) FROM DATABASECHANGELOG);
INSERT INTO DATABASECHANGELOG (id, author, filename, dateexecuted, orderexecuted, 
    md5sum, description, exectype, liquibase, deployment_id) 
VALUES ('xxx-create-table', 'author', 'db/changelog/changes/xxx.yaml', 
    NOW(), @max_order + 1, '8:generated', 'Create table', 'EXECUTED', 
    '4.27.0', LEFT(UUID(), 10));
```

---

### Q3: 外键约束失败

**症状:**
```
Cannot delete or update a parent row: a foreign key constraint fails
```

**原因:**
- 尝试删除被其他表引用的数据

**解决:**
1. 检查外键依赖：
```sql
SELECT TABLE_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME = 'users' 
AND TABLE_SCHEMA = 'creditapp';
```

2. 先删除依赖表的数据，或修改 changelog 删除顺序

---

## 📁 Changelog 文件位置

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml    # 主配置文件
└── changes/
    ├── 001-initial-schema-mysql.yaml
    ├── 002-initial-data.yaml
    ├── 024-add-word-study-rewards.sql
    ├── 026-add-story-download-reward.sql
    └── 027-add-math-exercise-task.sql
```

---

## 🛡️ 最佳实践

### ✅ 推荐做法

1. **使用版本号前缀** - `001-`, `002-`, `003-`
2. **小步提交** - 每个 changeset 只做一件事
3. **测试后再部署** - 开发环境验证后再上生产
4. **备份数据库** - 更新前务必备份
5. **使用 validate** - 每次更新前验证

### ❌ 避免的做法

1. **不要修改已执行的 changeset** - 创建新的
2. **不要手动修改 DATABASECHANGELOG** - 除非必要
3. **不要跳过验证** - `liquibase:validate` 很重要
4. **不要强制推送** - 保持数据库历史一致

---

## 📊 有用的命令

```bash
# 验证 changelog
mvn liquibase:validate

# 查看状态
mvn liquibase:status

# 查看历史记录
mvn liquibase:history

# 执行更新
mvn liquibase:update

# 清除 checksum
mvn liquibase:clearCheckSums

# 生成变更 SQL（不执行）
mvn liquibase:updateSQL

# 回滚（慎用）
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

---

## 🔗 相关资源

- [Liquibase 官方文档](https://docs.liquibase.com/)
- [Maven 插件文档](https://docs.liquibase.com/tools-integrations/maven/home.html)
- 项目文档：`/vol1/@apphome/trim.openclaw/data/workspace/MEMORY.md`

---

**最后更新:** 2026-04-06
