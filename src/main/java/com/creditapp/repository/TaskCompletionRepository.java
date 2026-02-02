package com.creditapp.repository;

import com.creditapp.entity.CompletionStatus;
import com.creditapp.entity.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {
    List<TaskCompletion> findByChildId(Long childId);
    List<TaskCompletion> findByStatus(CompletionStatus status);
    
    // Find recent task completions for parent's children
    @Query("SELECT tc FROM TaskCompletion tc " +
           "JOIN FETCH tc.child c " +
           "JOIN FETCH tc.task t " +
           "WHERE c.parent.id = :parentId " +
           "AND tc.status = 'APPROVED' " +
           "ORDER BY tc.completedAt DESC")
    List<TaskCompletion> findRecentCompletionsByParentId(@org.springframework.data.repository.query.Param("parentId") Long parentId);
    
    // Find recent task completions with limit
    @Query("SELECT tc FROM TaskCompletion tc " +
           "JOIN FETCH tc.child c " +
           "JOIN FETCH tc.task t " +
           "WHERE c.parent.id = :parentId " +
           "AND tc.status = 'APPROVED' " +
           "ORDER BY tc.completedAt DESC")
    List<TaskCompletion> findRecentCompletionsByParentIdWithLimit(
            @org.springframework.data.repository.query.Param("parentId") Long parentId,
            org.springframework.data.domain.Pageable pageable);
    
    // Count task completions by child for a parent
    @Query("SELECT c.id, c.username, COUNT(tc) as completionCount " +
           "FROM TaskCompletion tc " +
           "JOIN tc.child c " +
           "WHERE c.parent.id = :parentId " +
           "AND tc.status = 'APPROVED' " +
           "GROUP BY c.id, c.username")
    List<Object[]> countCompletionsByChildForParent(@org.springframework.data.repository.query.Param("parentId") Long parentId);
    
    // Get daily task completion statistics for the last 7 days
    @Query("SELECT CAST(tc.completedAt AS date) as completionDate, COUNT(tc) as taskCount, SUM(t.points) as pointsEarned " +
           "FROM TaskCompletion tc " +
           "JOIN tc.task t " +
           "JOIN tc.child c " +
           "WHERE c.parent.id = :parentId " +
           "AND tc.status = 'APPROVED' " +
           "AND tc.completedAt >= :startDate " +
           "GROUP BY CAST(tc.completedAt AS date) " +
           "ORDER BY completionDate")
    List<Object[]> getDailyTaskCompletionStats(
             @org.springframework.data.repository.query.Param("parentId") Long parentId,
             @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate);
    
    // Find pending task completions for parent's children
    @Query("SELECT tc FROM TaskCompletion tc " +
           "JOIN FETCH tc.child c " +
           "JOIN FETCH tc.task t " +
           "WHERE c.parent.id = :parentId " +
           "AND tc.status = 'PENDING' " +
           "ORDER BY tc.completedAt DESC")
    List<TaskCompletion> findPendingCompletionsByParentId(@org.springframework.data.repository.query.Param("parentId") Long parentId);
    
    // Check if child has completed a specific task today (for DAILY_ONCE validation)
    @Query("SELECT COUNT(tc) > 0 " +
           "FROM TaskCompletion tc " +
           "WHERE tc.child.id = :childId " +
           "AND tc.task.id = :taskId " +
           "AND tc.status IN ('PENDING', 'APPROVED') " +
           "AND tc.completedAt >= :startOfDay " +
           "AND tc.completedAt < :endOfDay")
    boolean existsCompletionToday(
            @org.springframework.data.repository.query.Param("childId") Long childId,
            @org.springframework.data.repository.query.Param("taskId") Long taskId,
            @org.springframework.data.repository.query.Param("startOfDay") java.time.LocalDateTime startOfDay,
            @org.springframework.data.repository.query.Param("endOfDay") java.time.LocalDateTime endOfDay);
}
