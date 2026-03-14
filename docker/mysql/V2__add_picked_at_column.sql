-- 添加领取时间字段
ALTER TABLE tasks ADD COLUMN picked_at DATETIME NULL COMMENT '领取任务的时间';

-- 更新已有的市场任务，设置 picked_at 为当前时间（如果有 picked_by_child_id）
UPDATE tasks SET picked_at = NOW() WHERE picked_by_child_id IS NOT NULL AND picked_at IS NULL;
