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
}
