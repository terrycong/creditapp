-- ============================================================================
-- TaskJob Refactoring Migration Script
-- Phase: V2 - TaskJob Architecture Implementation
-- ============================================================================
-- This script creates the task_jobs table and migrates existing data
-- Execute this AFTER the application is updated with the new entities
-- ============================================================================

-- ============================================================================
-- PHASE 1: Create task_jobs table (Backward Compatible)
-- ============================================================================

CREATE TABLE task_jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    child_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    snapshot_title VARCHAR(100) NOT NULL,
    snapshot_description VARCHAR(500),
    snapshot_points INT NOT NULL,
    snapshot_task_type VARCHAR(20) NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at DATETIME,
    deadline DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key constraints
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,

    -- Indexes for performance
    INDEX idx_task_jobs_child_status (child_id, status),
    INDEX idx_task_jobs_task_child (task_id, child_id),
    INDEX idx_task_jobs_assigned_at (assigned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- PHASE 2: Add task_job_id column to task_completions (Optional)
-- ============================================================================

ALTER TABLE task_completions
ADD COLUMN task_job_id BIGINT NULL AFTER task_id;

ALTER TABLE task_completions
ADD FOREIGN KEY (task_job_id) REFERENCES task_jobs(id) ON DELETE SET NULL;

CREATE INDEX idx_task_completions_task_job ON task_completions(task_job_id);

-- ============================================================================
-- PHASE 3: Data Migration - Migrate existing assignments to task_jobs
-- ============================================================================

-- 3.1 Migrate directly assigned tasks (assigned_child_id is not null)
INSERT INTO task_jobs (task_id, child_id, status, snapshot_title, snapshot_description,
                       snapshot_points, snapshot_task_type, assigned_at, created_at)
SELECT
    t.id AS task_id,
    t.assigned_child_id AS child_id,
    'ASSIGNED' AS status,
    t.title AS snapshot_title,
    t.description AS snapshot_description,
    t.points AS snapshot_points,
    t.type AS snapshot_task_type,
    NOW() AS assigned_at,
    NOW() AS created_at
FROM tasks t
WHERE t.assigned_child_id IS NOT NULL
  AND t.active = TRUE
  AND t.status = 'APPROVED'
  AND NOT EXISTS (
    SELECT 1 FROM task_jobs tj
    WHERE tj.task_id = t.id AND tj.child_id = t.assigned_child_id
  );

-- 3.2 Migrate marketplace picked tasks (picked_by_child_id is not null)
INSERT INTO task_jobs (task_id, child_id, status, snapshot_title, snapshot_description,
                       snapshot_points, snapshot_task_type, assigned_at, created_at)
SELECT
    t.id AS task_id,
    t.picked_by_child_id AS child_id,
    'ASSIGNED' AS status,
    t.title AS snapshot_title,
    t.description AS snapshot_description,
    t.points AS snapshot_points,
    t.type AS snapshot_task_type,
    NOW() AS assigned_at,
    NOW() AS created_at
FROM tasks t
WHERE t.picked_by_child_id IS NOT NULL
  AND t.active = TRUE
  AND t.status = 'APPROVED'
  AND t.assigned_child_id IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM task_jobs tj
    WHERE tj.task_id = t.id AND tj.child_id = t.picked_by_child_id
  );

-- ============================================================================
-- PHASE 4: Link existing task_completions to task_jobs
-- ============================================================================

-- 4.1 Update task_completions with task_job_id (for completions with jobs)
UPDATE task_completions tc
JOIN task_jobs tj ON tc.task_id = tj.task_id AND tc.child_id = tj.child_id
SET tc.task_job_id = tj.id
WHERE tc.task_job_id IS NULL;

-- ============================================================================
-- PHASE 5: Update task_jobs status based on task_completions
-- ============================================================================

-- 5.1 Mark jobs as COMPLETED if they have APPROVED completions
UPDATE task_jobs tj
JOIN task_completions tc ON tj.id = tc.task_job_id
SET tj.status = 'COMPLETED'
WHERE tc.status = 'APPROVED'
  AND tj.status = 'ASSIGNED';

-- 5.2 Mark jobs as IN_PROGRESS if they have PENDING completions
UPDATE task_jobs tj
JOIN task_completions tc ON tj.id = tc.task_job_id
SET tj.status = 'IN_PROGRESS'
WHERE tc.status = 'PENDING'
  AND tj.status = 'ASSIGNED';

-- ============================================================================
-- PHASE 6: Remove deprecated columns from tasks table
-- ============================================================================

-- IMPORTANT: Only run this AFTER application is updated to use TaskJob

ALTER TABLE tasks DROP FOREIGN KEY IF EXISTS fk_tasks_assigned_child;
ALTER TABLE tasks DROP FOREIGN KEY IF EXISTS fk_tasks_picked_by_child;

ALTER TABLE tasks DROP INDEX IF EXISTS idx_tasks_assigned_child;
ALTER TABLE tasks DROP INDEX IF EXISTS idx_tasks_picked_by_child;

ALTER TABLE tasks DROP COLUMN IF EXISTS assigned_child_id;
ALTER TABLE tasks DROP COLUMN IF EXISTS picked_by_child_id;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Check data consistency
SELECT '=== Task Jobs Summary ===' AS info;
SELECT status, COUNT(*) AS count FROM task_jobs GROUP BY status;

SELECT '=== Assigned vs TaskJobs ===' AS info;
SELECT
    'Directly Assigned Tasks' AS category,
    COUNT(*) AS count
FROM tasks
WHERE assigned_child_id IS NOT NULL AND active = TRUE AND status = 'APPROVED'
UNION ALL
SELECT
    'TaskJobs from Assignments' AS category,
    COUNT(*) AS count
FROM task_jobs;

SELECT '=== Marketplace Picked vs TaskJobs ===' AS info;
SELECT
    'Picked Marketplace Tasks' AS category,
    COUNT(*) AS count
FROM tasks
WHERE picked_by_child_id IS NOT NULL AND active = TRUE AND status = 'APPROVED'
UNION ALL
SELECT
    'TaskJobs from Marketplace' AS category,
    COUNT(*) AS count
FROM task_jobs tj
JOIN tasks t ON tj.task_id = t.id
WHERE t.assigned_child_id IS NULL;

SELECT '=== TaskCompletions Linking ===' AS info;
SELECT
    'Total TaskCompletions' AS category,
    COUNT(*) AS count
FROM task_completions
UNION ALL
SELECT
    'Completions with TaskJob' AS category,
    COUNT(*) AS count
FROM task_completions
WHERE task_job_id IS NOT NULL;

-- Check for orphaned records
SELECT '=== Orphan Check ===' AS info;
SELECT
    'Orphaned TaskJobs (no task)' AS category,
    COUNT(*) AS count
FROM task_jobs tj
LEFT JOIN tasks t ON tj.task_id = t.id
WHERE t.id IS NULL;
