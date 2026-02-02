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
    List<Task> findByAssignedChild_Id(Long childId);
    List<Task> findByTypeAndActive(TaskType type, boolean active);
    List<Task> findByStatusAndAssignedChild(TaskStatus status, Child child);

    @Query("SELECT t FROM Task t JOIN FETCH t.assignedChild WHERE t.active = true AND t.assignedChild.id = :childId")
    List<Task> findActiveTasksWithChild(@Param("childId") Long childId);

    // Find DRAFT tasks created by children (for parent approval)
    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.assignedChild c " +
           "WHERE c.parent.id = :parentId " +
           "AND t.status = 'DRAFT' " +
           "ORDER BY t.id DESC")
    List<Task> findDraftTasksByParentId(@Param("parentId") Long parentId);

    // Find tasks created by a specific child
    @Query("SELECT t FROM Task t WHERE t.createdBy.id = :childId ORDER BY t.id DESC")
    List<Task> findByCreatedById(@Param("childId") Long childId);
}
