--liquibase formatted sql

--changeset admin:schema-rewards-redemption-add-note
--comment: Add note column to reward_redemptions table
-- 修复：RewardRedemption 实体类中有 note 字段，但数据库表缺少该列

ALTER TABLE reward_redemptions 
ADD COLUMN note VARCHAR(500) NULL COMMENT 'Redemption note or comment' AFTER redeemed_at;
