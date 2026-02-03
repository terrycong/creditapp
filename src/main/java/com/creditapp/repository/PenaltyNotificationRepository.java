package com.creditapp.repository;

import com.creditapp.entity.NotificationStatus;
import com.creditapp.entity.PenaltyNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PenaltyNotificationRepository extends JpaRepository<PenaltyNotification, Long> {

    // Find pending notifications for a parent
    List<PenaltyNotification> findByParentIdAndStatusOrderByNotificationTimeDesc(Long parentId, NotificationStatus status);

    // Find all notifications for a parent
    List<PenaltyNotification> findByParentIdOrderByNotificationTimeDesc(Long parentId);

    // Find notifications for a specific child
    List<PenaltyNotification> findByChildIdOrderByNotificationTimeDesc(Long childId);
    
    // Find notifications by task ID (for cascade delete)
    List<PenaltyNotification> findByTaskId(Long taskId);

    // Check if notification already exists for this task and child (avoid duplicates)
    @Query("SELECT COUNT(pn) > 0 FROM PenaltyNotification pn " +
           "WHERE pn.task.id = :taskId " +
           "AND pn.child.id = :childId " +
           "AND pn.status = 'PENDING'")
    boolean existsPendingNotification(@Param("taskId") Long taskId, @Param("childId") Long childId);

    // Count pending notifications for parent
    long countByParentIdAndStatus(Long parentId, NotificationStatus status);
}
