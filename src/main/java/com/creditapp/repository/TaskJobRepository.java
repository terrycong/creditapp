package com.creditapp.repository;

import com.creditapp.entity.JobStatus;
import com.creditapp.entity.Task;
import com.creditapp.entity.TaskJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TaskJob entity
 * Handles all queries related to task assignments/instances for children.
 */
@Repository
public interface TaskJobRepository extends JpaRepository<TaskJob, Long> {

    /**
     * Find all active jobs for a child (ASSIGNED or IN_PROGRESS status)
     * These represent "进行中任务" (tasks in progress)
     */
    @Query("SELECT tj FROM TaskJob tj JOIN FETCH tj.task WHERE tj.child.id = :childId " +
           "AND tj.status IN ('ASSIGNED', 'IN_PROGRESS') ORDER BY tj.assignedAt DESC")
    List<TaskJob> findActiveJobsByChildId(@Param("childId") Long childId);

    /**
     * Count active jobs for a child
     * Used for displaying "进行中任务" count
     */
    @Query("SELECT COUNT(tj) FROM TaskJob tj WHERE tj.child.id = :childId " +
           "AND tj.status IN ('ASSIGNED', 'IN_PROGRESS')")
    int countActiveJobsByChildId(@Param("childId") Long childId);

    /**
     * Find a job by task and child combination
     */
    Optional<TaskJob> findByTaskIdAndChildId(Long taskId, Long childId);

    /**
     * Find all jobs for a specific task (for parent view)
     */
    List<TaskJob> findByTaskId(Long taskId);

    /**
     * Find all jobs for a specific child
     */
    List<TaskJob> findByChildId(Long childId);

    /**
     * Find jobs by status for a child
     */
    List<TaskJob> findByChildIdAndStatus(Long childId, JobStatus status);

    /**
     * Find marketplace-available tasks (tasks with no active job for this parent)
     * Returns tasks that are approved, active, and not currently assigned to any child
     */
    @Query("SELECT t FROM Task t WHERE t.active = true AND t.status = 'APPROVED' " +
           "AND t.createdBy.id = :parentId " +
           "AND NOT EXISTS (SELECT tj FROM TaskJob tj WHERE tj.task = t AND tj.status IN ('ASSIGNED', 'IN_PROGRESS'))")
    List<Task> findAvailableMarketplaceTasks(@Param("parentId") Long parentId);

    /**
     * Find all jobs picked by a child from marketplace
     * Marketplace tasks are identified by NOT having direct assignment
     * Since all assignments now go through TaskJob, we return all active jobs
     */
    @Query("SELECT tj FROM TaskJob tj JOIN FETCH tj.task WHERE tj.child.id = :childId " +
           "AND tj.status IN ('ASSIGNED', 'IN_PROGRESS')")
    List<TaskJob> findPickedJobsByChildId(@Param("childId") Long childId);

    /**
     * Check if a child already has an active job for a specific task
     */
    @Query("SELECT CASE WHEN COUNT(tj) > 0 THEN true ELSE false END FROM TaskJob tj " +
           "WHERE tj.task.id = :taskId AND tj.child.id = :childId " +
           "AND tj.status IN ('ASSIGNED', 'IN_PROGRESS')")
    boolean existsActiveJobByTaskIdAndChildId(@Param("taskId") Long taskId, @Param("childId") Long childId);

    /**
     * Find completed jobs for a child (for history)
     */
    @Query("SELECT tj FROM TaskJob tj JOIN FETCH tj.task WHERE tj.child.id = :childId " +
           "AND tj.status = 'COMPLETED' ORDER BY tj.assignedAt DESC")
    List<TaskJob> findCompletedJobsByChildId(@Param("childId") Long childId);

    /**
     * Find jobs created by a parent (for parent dashboard)
     */
    @Query("SELECT tj FROM TaskJob tj JOIN FETCH tj.task JOIN FETCH tj.child " +
           "WHERE tj.task.createdBy.id = :parentId ORDER BY tj.assignedAt DESC")
    List<TaskJob> findJobsByParentId(@Param("parentId") Long parentId);

    /**
     * Find active jobs created by a parent (for parent dashboard)
     */
    @Query("SELECT tj FROM TaskJob tj JOIN FETCH tj.task JOIN FETCH tj.child " +
           "WHERE tj.task.createdBy.id = :parentId AND tj.status IN ('ASSIGNED', 'IN_PROGRESS') " +
           "ORDER BY tj.assignedAt DESC")
    List<TaskJob> findActiveJobsByParentId(@Param("parentId") Long parentId);

    /**
     * Count jobs by status for a parent
     */
    @Query("SELECT tj.status, COUNT(tj) FROM TaskJob tj JOIN tj.task t " +
           "WHERE t.createdBy.id = :parentId GROUP BY tj.status")
    List<Object[]> countJobsByStatusForParent(@Param("parentId") Long parentId);
}
