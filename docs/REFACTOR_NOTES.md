# Task Entity 字段移除 - 修改清单

## 已完成的修改

### 1. Task.java
- ✅ 移除 `assignedChild` 字段
- ✅ 移除 `pickedByChild` 字段  
- ✅ 移除 `pickedAt` 字段
- ✅ 移除相关 getter/setter

### 2. TaskDTO.java
- ✅ 移除 `assignedChildId` 字段
- ✅ 移除 `assignedChildName` 字段
- ✅ 移除 `pickedByChildId` 字段
- ✅ 移除 `pickedByChildName` 字段
- ✅ 移除 `pickedAt` 字段
- ✅ 移除相关 getter/setter 和 Builder 方法

### 3. TaskRepository.java
- ✅ 修改 `findAvailableMarketplaceTasksByParentId` - 使用 NOT EXISTS TaskJob 判断
- ✅ 修改 `findVisibleMarketplaceTasksByParentIdWithSearch` - 添加 NOT EXISTS TaskJob 条件
- ✅ 修改 `findVisibleMarketplaceTasksByParentId` - 添加 NOT EXISTS TaskJob 条件
- ✅ 修改 `findPickedTasksByChildId` - 通过 TaskJob JOIN 查询
- ✅ 修改 `findActiveTasksWithChild` - 通过 TaskJob JOIN 查询

## 需要手动修复的文件

### TaskServiceImpl.java (需要修复 8 处)

**行 192-193**: 移除 assignedChildId/assignedChildName
```java
// 删除这两行：
.assignedChildId(job.getChild().getId())
.assignedChildName(job.getChild().getUsername())
```

**行 562**: 移除 pickedAt
```java
// 删除：
.pickedAt(task.getPickedAt())
```

**行 581**: 移除 pickedAt
```java
// 删除：
.pickedAt(completion.getTask().getPickedAt())
```

**行 632-636**: 移除 setPickedByChild 和 setPickedAt
```java
// 删除这 4 行：
task.setPickedByChild(child);
task.setPickedAt(LocalDateTime.now());
taskRepository.save(task);
log.info("Task {} picked by child {} at {}", taskId, childId, task.getPickedAt());

// 替换为：
log.info("Task {} picked by child {}", taskId, childId);
```

### ViewController.java (需要修复 2 处)

**行 171**: 移除 getPickedByChildId 判断
```java
// 查找这行并删除相关判断：
task.pickedByChildId != null

// 由于市场任务通过 TaskJob 判断是否被领取，前端不需要显示"已领取"状态
```

**行 1023**: 同上

## 前端模板需要修改

### child/tasks.html
- 移除 `task.pickedByChildId` 的显示逻辑
- 移除"已领取"徽章显示

### child/marketplace.html  
- 移除 `task.pickedByChildId` 的显示逻辑
- 修改为通过 TaskJob 判断任务是否已被领取

## 数据库迁移

由于 `tasks` 表中删除了 `assigned_child_id` 和 `picked_by_child_id` 列，需要：

1. 如果使用 H2 数据库（开发环境）- 重启应用会自动重建表
2. 如果使用 MySQL（生产环境）- 需要手动执行：
```sql
ALTER TABLE tasks DROP COLUMN assigned_child_id;
ALTER TABLE tasks DROP COLUMN picked_by_child_id;
```

## 设计理念

**之前**（错误）：
- Task 实体同时承担"任务模板"和"任务实例"两种角色
- `assignedChild` 和 `pickedByChild` 语义混淆

**现在**（正确）：
- **Task** = 任务模板/定义（家长创建的任务，放在市场上）
- **TaskJob** = 任务实例（孩子领取后创建的关联记录）
- 一个 Task 可以对应多个 TaskJob（多个孩子可以领取同一个市场任务？待确认）

## 待确认问题

1. 市场任务是否应该"一个孩子只能领取一次"？
   - 如果是：TaskJob 需要唯一约束 (task_id, child_id)
   - 如果否：多个孩子可以领取同一个任务（适合家务任务）

2. 直接分配任务如何处理？
   - 家长创建任务时选择孩子 → 创建 Task 和 TaskJob
   - 还是：只创建 TaskJob，不创建 Task？

建议：家长创建任务时，如果选择了孩子，同时创建 Task 和 TaskJob。
