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
}
