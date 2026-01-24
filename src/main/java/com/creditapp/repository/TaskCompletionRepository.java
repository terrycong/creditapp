package com.creditapp.repository;

import com.creditapp.entity.CompletionStatus;
import com.creditapp.entity.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {
    List<TaskCompletion> findByChildId(Long childId);
    List<TaskCompletion> findByStatus(CompletionStatus status);
}
