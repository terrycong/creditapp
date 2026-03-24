-- Liquibase SQL Changelog for Initial Schema
-- ChangeSet: 001-initial-schema

-- Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('CHILD','PARENT') NOT NULL,
    points INT DEFAULT 0 NOT NULL,
    parent_id BIGINT
);

-- Children Table  
CREATE TABLE children (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('CHILD','PARENT') NOT NULL,
    points INT DEFAULT 0 NOT NULL,
    parent_id BIGINT NOT NULL,
    CONSTRAINT FK_children_parent FOREIGN KEY (parent_id) REFERENCES users(id)
);

-- Tasks Table
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    points INT NOT NULL,
    type ENUM('DAILY_ONCE','MANDATORY','ONE_TIME','REPEATABLE') NOT NULL,
    status ENUM('APPROVED','DRAFT','REJECTED'),
    active BOOLEAN DEFAULT TRUE NOT NULL,
    penalty_points INT,
    deadline_type ENUM('DAILY','WEEKLY_TIMES'),
    deadline_value INT,
    created_by_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

-- Task Jobs Table
CREATE TABLE task_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    status ENUM('ASSIGNED','CANCELLED','COMPLETED','IN_PROGRESS') NOT NULL,
    snapshot_title VARCHAR(100) NOT NULL,
    snapshot_description VARCHAR(500),
    snapshot_points INT NOT NULL,
    snapshot_task_type ENUM('DAILY_ONCE','MANDATORY','ONE_TIME','REPEATABLE') NOT NULL,
    assigned_at DATETIME NOT NULL,
    started_at DATETIME,
    deadline DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

-- Task Completions Table
CREATE TABLE task_completions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    task_job_id BIGINT,
    child_id BIGINT NOT NULL,
    status ENUM('APPROVED','PENDING','REJECTED') NOT NULL,
    proof TEXT,
    completed_at DATETIME NOT NULL,
    approved_at DATETIME
);

-- Rewards Table
CREATE TABLE rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    quantity INT DEFAULT 999 NOT NULL,
    points_required INT NOT NULL,
    image_url VARCHAR(500),
    active BOOLEAN DEFAULT TRUE NOT NULL
);

-- Reward Redemptions Table
CREATE TABLE reward_redemptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reward_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    status ENUM('REDEEMED','USED') NOT NULL,
    redeemed_at DATETIME NOT NULL,
    used_at DATETIME,
    note VARCHAR(500)
);

-- Lottery Themes Table
CREATE TABLE lottery_themes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    points_per_draw INT NOT NULL,
    type ENUM('FIXED_PROBABILITY','GUARANTEED','WEIGHTED_RANDOM') NOT NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_by_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

-- Lottery Prizes Table
CREATE TABLE lottery_prizes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lottery_theme_id BIGINT NOT NULL,
    reward_id BIGINT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(500),
    weight INT NOT NULL,
    probability INT,
    quantity INT DEFAULT -1 NOT NULL,
    redeemed_count INT DEFAULT 0 NOT NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

-- Lottery Draws Table
CREATE TABLE lottery_draws (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lottery_theme_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    points_cost INT NOT NULL,
    result_status ENUM('JACKPOT','NO_WIN','WON') NOT NULL,
    draw_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL
);

-- Lottery Draw Results Table
CREATE TABLE lottery_draw_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lottery_draw_id BIGINT NOT NULL,
    prize_id BIGINT NOT NULL,
    reward_id BIGINT,
    prize_name VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL
);

-- Point History Table
CREATE TABLE point_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    change_type ENUM('BONUS','INITIAL','LOTTERY_DRAW','LOTTERY_WIN','MANUAL_ADJUSTMENT','OTHER','PENALTY','REWARD_REDEMPTION','TASK_COMPLETION') NOT NULL,
    change_points INT NOT NULL,
    original_points INT NOT NULL,
    after_points INT NOT NULL,
    description VARCHAR(500),
    reference_id BIGINT,
    reference_type VARCHAR(50),
    changed_by_id BIGINT,
    created_at DATETIME NOT NULL
);

-- Penalty Rules Table
CREATE TABLE penalty_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    points INT NOT NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_by_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

-- Penalty Records Table
CREATE TABLE penalty_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    penalty_rule_id BIGINT NOT NULL,
    points INT NOT NULL,
    note VARCHAR(500),
    applied_by_id BIGINT NOT NULL,
    applied_at DATETIME NOT NULL
);

-- Penalty Notifications Table
CREATE TABLE penalty_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    child_id BIGINT NOT NULL,
    parent_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    status ENUM('APPLIED','DISMISSED','EXPIRED','PENDING') NOT NULL,
    notification_time DATETIME NOT NULL,
    penalty_points INT,
    is_penalty_applied BOOLEAN,
    applied_by_id BIGINT,
    applied_at DATETIME
);
