--liquibase formatted sql

--changeset admin:schema-01-users
--comment: Create users and children tables
-- 1. 用户系统表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('CHILD', 'PARENT') NOT NULL,
    points INT DEFAULT 0 NOT NULL,
    parent_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User accounts (parents and children)';

CREATE TABLE IF NOT EXISTS children (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('CHILD', 'PARENT') NOT NULL,
    points INT DEFAULT 0 NOT NULL,
    parent_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_children_parent FOREIGN KEY (parent_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_children_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Child accounts with parent relationship';

-- ============================================================
-- 2. 任务系统表
-- ============================================================

--changeset admin:schema-02-tasks
--comment: Create tasks, task_jobs, and task_completions tables
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    points INT NOT NULL,
    type ENUM('DAILY_ONCE', 'MANDATORY', 'ONE_TIME', 'REPEATABLE') NOT NULL,
    status ENUM('APPROVED', 'DRAFT', 'REJECTED') DEFAULT 'APPROVED',
    active BIT(1) DEFAULT b'1' NOT NULL,
    penalty_points INT,
    deadline_type ENUM('DAILY', 'WEEKLY_TIMES'),
    deadline_value INT,
    created_by_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_tasks_created_by FOREIGN KEY (created_by_id) REFERENCES users(id),
    INDEX idx_tasks_type (type),
    INDEX idx_tasks_status (status),
    INDEX idx_tasks_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Task definitions created by parents';

CREATE TABLE IF NOT EXISTS task_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    status ENUM('ASSIGNED', 'IN_PROGRESS', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'ASSIGNED',
    snapshot_title VARCHAR(100) NOT NULL,
    snapshot_description VARCHAR(500),
    snapshot_points INT NOT NULL,
    snapshot_task_type ENUM('DAILY_ONCE', 'MANDATORY', 'ONE_TIME', 'REPEATABLE') NOT NULL,
    assigned_at DATETIME NOT NULL,
    completed_at DATETIME,
    approved_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_task_jobs_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT FK_task_jobs_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,
    INDEX idx_task_jobs_status (status),
    INDEX idx_task_jobs_child (child_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Task instances assigned to children';

CREATE TABLE IF NOT EXISTS task_completions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    task_job_id BIGINT,
    child_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    proof VARCHAR(2000),
    completed_at DATETIME NOT NULL,
    approved_at DATETIME,
    CONSTRAINT FK_task_completions_task FOREIGN KEY (task_id) REFERENCES tasks(id),
    CONSTRAINT FK_task_completions_job FOREIGN KEY (task_job_id) REFERENCES task_jobs(id),
    CONSTRAINT FK_task_completions_child FOREIGN KEY (child_id) REFERENCES children(id),
    INDEX idx_completions_child (child_id),
    INDEX idx_completions_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Task completion records with proof';

-- ============================================================
-- 3. 奖励系统表
-- ============================================================

--changeset admin:schema-03-rewards
--comment: Create rewards and reward_redemptions tables
CREATE TABLE IF NOT EXISTS rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    quantity INT DEFAULT 999 NOT NULL,
    points_required INT NOT NULL,
    active BIT(1) DEFAULT b'1' NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_rewards_active (active),
    INDEX idx_rewards_points (points_required)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Reward items redeemable with points';

CREATE TABLE IF NOT EXISTS reward_redemptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reward_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    total_points INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'FULFILLED') DEFAULT 'PENDING',
    redeemed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at DATETIME,
    approved_by_id BIGINT,
    CONSTRAINT FK_reward_redemptions_reward FOREIGN KEY (reward_id) REFERENCES rewards(id),
    CONSTRAINT FK_reward_redemptions_child FOREIGN KEY (child_id) REFERENCES children(id),
    INDEX idx_redemptions_status (status),
    INDEX idx_redemptions_child (child_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Reward redemption requests';

-- ============================================================
-- 4. 抽奖系统表
-- ============================================================

--changeset admin:schema-04-lottery
--comment: Create lottery system tables
CREATE TABLE IF NOT EXISTS lottery_themes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    points_per_draw INT NOT NULL DEFAULT 10,
    type ENUM('WEIGHTED_RANDOM', 'UNIFORM_RANDOM') NOT NULL DEFAULT 'WEIGHTED_RANDOM',
    active BIT(1) DEFAULT b'1' NOT NULL,
    created_by_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_lottery_themes_creator FOREIGN KEY (created_by_id) REFERENCES users(id),
    INDEX idx_lottery_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lottery theme configurations';

CREATE TABLE IF NOT EXISTS lottery_prizes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lottery_theme_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    weight INT NOT NULL DEFAULT 10,
    quantity INT DEFAULT 999 NOT NULL,
    redeemed_count INT DEFAULT 0 NOT NULL,
    active BIT(1) DEFAULT b'1' NOT NULL,
    reward_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_lottery_prizes_theme FOREIGN KEY (lottery_theme_id) REFERENCES lottery_themes(id) ON DELETE CASCADE,
    CONSTRAINT FK_lottery_prizes_reward FOREIGN KEY (reward_id) REFERENCES rewards(id),
    INDEX idx_prizes_theme (lottery_theme_id),
    INDEX idx_prizes_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Prizes within lottery themes';

CREATE TABLE IF NOT EXISTS lottery_draws (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lottery_theme_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    points_spent INT NOT NULL,
    draw_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_lottery_draws_theme FOREIGN KEY (lottery_theme_id) REFERENCES lottery_themes(id),
    CONSTRAINT FK_lottery_draws_child FOREIGN KEY (child_id) REFERENCES children(id),
    INDEX idx_draws_child (child_id),
    INDEX idx_draws_time (draw_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lottery draw records';

CREATE TABLE IF NOT EXISTS lottery_draw_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lottery_draw_id BIGINT NOT NULL,
    prize_id BIGINT NOT NULL,
    prize_name VARCHAR(100) NOT NULL,
    won_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_draw_results_draw FOREIGN KEY (lottery_draw_id) REFERENCES lottery_draws(id) ON DELETE CASCADE,
    CONSTRAINT FK_draw_results_prize FOREIGN KEY (prize_id) REFERENCES lottery_prizes(id),
    INDEX idx_results_draw (lottery_draw_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Results of lottery draws';

-- ============================================================
-- 5. 积分历史表
-- ============================================================

--changeset admin:schema-05-points
--comment: Create point_history table
CREATE TABLE IF NOT EXISTS point_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    change_amount INT NOT NULL,
    balance_after INT NOT NULL,
    change_type ENUM('TASK_COMPLETION', 'REWARD_REDEMPTION', 'LOTTERY', 'PENALTY', 'ADJUSTMENT', 'REFUND') NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    description VARCHAR(500),
    created_by_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT FK_point_history_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,
    CONSTRAINT FK_point_history_creator FOREIGN KEY (created_by_id) REFERENCES users(id),
    INDEX idx_history_child (child_id),
    INDEX idx_history_type (change_type),
    INDEX idx_history_time (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Point transaction history';

-- ============================================================
-- 6. 惩罚系统表
-- ============================================================

--changeset admin:schema-06-penalty
--comment: Create penalty system tables
CREATE TABLE IF NOT EXISTS penalty_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    points INT NOT NULL,
    active BIT(1) DEFAULT b'1' NOT NULL,
    created_by_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_penalty_rules_creator FOREIGN KEY (created_by_id) REFERENCES users(id),
    INDEX idx_penalty_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Penalty rule definitions';

CREATE TABLE IF NOT EXISTS penalty_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    penalty_rule_id BIGINT,
    reason VARCHAR(500) NOT NULL,
    points_deducted INT NOT NULL,
    applied_by_id BIGINT,
    applied_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT FK_penalty_records_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,
    CONSTRAINT FK_penalty_records_rule FOREIGN KEY (penalty_rule_id) REFERENCES penalty_rules(id),
    CONSTRAINT FK_penalty_records_applier FOREIGN KEY (applied_by_id) REFERENCES users(id),
    INDEX idx_penalty_child (child_id),
    INDEX idx_penalty_time (applied_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Penalty application records';

CREATE TABLE IF NOT EXISTS penalty_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    parent_id BIGINT NOT NULL,
    notification_time DATETIME NOT NULL,
    status VARCHAR(50) NOT NULL,
    penalty_points INT,
    is_penalty_applied TINYINT DEFAULT 0,
    applied_at DATETIME,
    applied_by_id BIGINT,
    CONSTRAINT fk_penalty_notifications_child FOREIGN KEY (child_id) REFERENCES children(id),
    CONSTRAINT fk_penalty_notifications_parent FOREIGN KEY (parent_id) REFERENCES users(id),
    CONSTRAINT fk_penalty_notifications_task FOREIGN KEY (task_id) REFERENCES tasks(id),
    INDEX idx_penaltynotif_child (child_id),
    INDEX idx_penaltynotif_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Penalty notification tracking';

-- ============================================================
-- 7. 通知系统表
-- ============================================================

--changeset admin:schema-07-notifications
--comment: Create notifications tables
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('TASK', 'REWARD', 'PENALTY', 'SYSTEM') NOT NULL,
    is_read BIT(1) DEFAULT b'0' NOT NULL,
    created_by_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    read_at DATETIME,
    CONSTRAINT FK_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_created_by FOREIGN KEY (created_by_id) REFERENCES users(id),
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_read (is_read),
    INDEX idx_notifications_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User notifications';

CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    task_reminder BIT(1) DEFAULT b'1' NOT NULL,
    reward_approved BIT(1) DEFAULT b'1' NOT NULL,
    penalty_applied BIT(1) DEFAULT b'1' NOT NULL,
    system_announcement BIT(1) DEFAULT b'1' NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_notification_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User notification preferences';

-- ============================================================
-- 8. 反馈系统表
-- ============================================================

--changeset admin:schema-08-feedback
--comment: Create feedback table for child suggestions
CREATE TABLE IF NOT EXISTS feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    category ENUM('FUNCTIONAL', 'NON_FUNCTIONAL', 'OTHER') NOT NULL COMMENT 'FUNCTIONAL=功能建议，NON_FUNCTIONAL=非功能建议，OTHER=其他',
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status ENUM('PENDING', 'REVIEWED', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING' NOT NULL,
    parent_response TEXT,
    responded_by_id BIGINT,
    responded_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT FK_feedbacks_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,
    CONSTRAINT FK_feedbacks_responder FOREIGN KEY (responded_by_id) REFERENCES users(id),
    INDEX idx_feedbacks_child (child_id),
    INDEX idx_feedbacks_status (status),
    INDEX idx_feedbacks_category (category),
    INDEX idx_feedbacks_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Child feedback and suggestions';

-- ============================================================
-- 9. 优惠券系统表
-- ============================================================

--changeset admin:schema-09-coupons
--comment: Create coupons table
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    discount_type ENUM('PERCENTAGE', 'FIXED_POINTS') NOT NULL,
    discount_value INT NOT NULL,
    min_points_required INT DEFAULT 0,
    max_discount INT,
    valid_from DATETIME,
    valid_until DATETIME,
    usage_limit INT,
    used_count INT DEFAULT 0 NOT NULL,
    active BIT(1) DEFAULT b'1' NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_coupons_code (code),
    INDEX idx_coupons_active (active),
    INDEX idx_coupons_validity (valid_from, valid_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Discount coupons for rewards';

-- ============================================================
-- Schema creation complete - 9 sections, 18 tables
-- ============================================================
