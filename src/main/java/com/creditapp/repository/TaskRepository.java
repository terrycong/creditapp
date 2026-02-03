package com.creditapp.repository;

import com.creditapp.entity.Child;
import com.creditapp.entity.Task;
import com.creditapp.entity.TaskStatus;
import com.creditapp.entity.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCreatedBy_Id(Long userId);
    List<Task> findByTypeAndActive(TaskType type, boolean active);

    // Find DRAFT tasks created by children (for parent approval)
    @Query("SELECT t FROM Task t " +
           "WHERE t.createdBy.id IN (SELECT c.id FROM Child c WHERE c.parent.id = :parentId) " +
           "AND t.status = 'DRAFT' " +
           "ORDER BY t.id DESC")
    List<Task> findDraftTasksByParentId(@Param("parentId") Long parentId);

    // Find tasks created by a specific child
    @Query("SELECT t FROM Task t WHERE t.createdBy.id = :childId ORDER BY t.id DESC")
    List<Task> findByCreatedById(@Param("childId") Long childId);

    // Find all marketplace tasks for parent view
    // Tasks are marketplace tasks if they have no TaskJob assigned
    @Query("SELECT t FROM Task t " +
            "WHERE t.createdBy.id = :parentId " +
            "AND t.active = true " +
            "AND t.status = 'APPROVED' " +
            "AND NOT EXISTS (SELECT tj FROM TaskJob tj WHERE tj.task = t AND tj.status IN ('ASSIGNED', 'IN_PROGRESS')) " +
            "ORDER BY t.id DESC")
    List<Task> findAvailableMarketplaceTasksByParentId(@Param("parentId") Long parentId);

    // Find all marketplace tasks (available + picked) for parent view
    @Query("SELECT t FROM Task t " +
            "LEFT JOIN FETCH t.createdBy " +
            "WHERE t.createdBy.id = :parentId " +
            "AND t.active = true " +
            "AND t.status = 'APPROVED' " +
            "ORDER BY t.id DESC")
    List<Task> findAllMarketplaceTasksByParentId(@Param("parentId") Long parentId);

    // Find all marketplace tasks including hidden/inactive for parent management view
    @Query("SELECT t FROM Task t " +
            "LEFT JOIN FETCH t.createdBy " +
            "WHERE t.createdBy.id = :parentId " +
            "AND t.status = 'APPROVED' " +
            "ORDER BY t.active DESC, t.id DESC")
    List<Task> findAllMarketplaceTasksIncludingHiddenByParentId(@Param("parentId") Long parentId);

    // Find marketplace tasks visible to children (only active ones)
    @Query("SELECT t FROM Task t " +
            "LEFT JOIN FETCH t.createdBy " +
            "WHERE t.createdBy.id = :parentId " +
            "AND t.active = true " +
            "AND t.status = 'APPROVED' " +
            "ORDER BY t.id DESC")
    List<Task> findVisibleMarketplaceTasksByParentId(@Param("parentId") Long parentId);
}
