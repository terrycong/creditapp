--liquibase formatted sql

--changeset admin:023-create-notifications-tables
--comment: Create notifications and notification_reads tables for family messaging system

-- ============================================
-- Create notifications table
-- ============================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    title VARCHAR(200) NOT NULL COMMENT 'Notification title',
    content VARCHAR(1000) NOT NULL COMMENT 'Notification content',
    created_by_id BIGINT NOT NULL COMMENT 'Created by parent user ID',
    target_type ENUM('ALL', 'SPECIFIC_CHILD') DEFAULT 'ALL' COMMENT 'Target audience',
    target_child_id BIGINT COMMENT 'Target child ID (if SPECIFIC_CHILD)',
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') DEFAULT 'NORMAL' COMMENT 'Priority level',
    active BOOLEAN DEFAULT TRUE NOT NULL COMMENT 'Is active',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created timestamp',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated timestamp',
    CONSTRAINT fk_notifications_created_by FOREIGN KEY (created_by_id) REFERENCES users(id),
    CONSTRAINT fk_notifications_target_child FOREIGN KEY (target_child_id) REFERENCES children(id),
    INDEX idx_notifications_created_at (created_at DESC),
    INDEX idx_notifications_active (active),
    INDEX idx_notifications_target (target_type, target_child_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Family notifications';

-- ============================================
-- Create notification_reads table (tracking read status)
-- ============================================
CREATE TABLE IF NOT EXISTS notification_reads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    notification_id BIGINT NOT NULL COMMENT 'Notification ID',
    child_id BIGINT NOT NULL COMMENT 'Child user ID',
    read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Read timestamp',
    UNIQUE KEY uk_notification_child (notification_id, child_id),
    CONSTRAINT fk_reads_notification FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE,
    CONSTRAINT fk_reads_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,
    INDEX idx_reads_child (child_id),
    INDEX idx_reads_notification (notification_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Notification read status tracking';

-- Rollback comment: DROP TABLE notification_reads, notifications;
