--liquibase formatted sql

--changeset admin:032-remove-sitting-reward
--comment: Remove reward for staying seated during meals

-- ============================================
-- Delete reward: 一天吃饭不离开座位
-- ============================================

DELETE FROM rewards WHERE name = '一天吃饭不离开座位';

-- Rollback comment: This is a one-time data migration.
