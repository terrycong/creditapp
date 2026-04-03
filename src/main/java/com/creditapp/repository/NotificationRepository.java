package com.creditapp.repository;

import com.creditapp.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all active notifications for a specific child
     */
    @Query("SELECT n FROM Notification n WHERE n.active = true " +
           "AND (n.targetType = 'ALL' OR (n.targetType = 'SPECIFIC_CHILD' AND n.targetChildId = :childId)) " +
           "ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findAllForChild(@Param("childId") Long childId);

    /**
     * Find all notifications created by a parent
     */
    List<Notification> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);

    /**
     * Find all active notifications ordered by priority and creation date
     */
    @Query("SELECT n FROM Notification n WHERE n.active = true ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findAllActiveOrderByPriorityDesc();
}
