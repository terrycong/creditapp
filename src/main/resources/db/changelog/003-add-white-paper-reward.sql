-- Changelog: 003-add-white-paper-reward.sql
-- Date: 2026-04-17
-- Description: 添加新礼物 - 一张白纸 (1 积分)

INSERT INTO rewards (name, description, quantity, points_required, image_url, active)
VALUES (
    '一张白纸',
    '纯白纸张一张，可以用来画画、写字或折纸，发挥你的创意！',
    999,
    1,
    NULL,
    TRUE
);

-- @UNDO
-- DELETE FROM rewards WHERE name = '一张白纸' AND points_required = 1;
