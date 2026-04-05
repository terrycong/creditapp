--liquibase formatted sql

--changeset admin:025-add-video-with-dad-reward
--comment: Add reward: watch short video with dad

-- ============================================
-- 和爸爸看短视频奖励
-- ============================================

INSERT INTO rewards (name, description, quantity, points_required, active) 
VALUES ('和爸爸看一个10分钟内的短视频', '和爸爸一起看一个10分钟内的短视频。备注：要等爸爸有空哦，而且每天只能用3次。', 999, 10, true);

-- Rollback comment: This is a one-time data migration.