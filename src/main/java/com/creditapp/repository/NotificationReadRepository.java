package com.creditapp.repository;

import com.creditapp.entity.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {

    /**
     * Check if a notification has been read by a child
     */
    Optional<NotificationRead> findByNotificationIdAndChildId(Long notificationId, Long childId);

    /**
     * Find all read notifications for a child
     */
    List<NotificationRead> findByChildIdOrderByReadAtDesc(Long childId);

    /**
     * Find all notification IDs that have been read by a child
     */
    @Query("SELECT nr.notificationId FROM NotificationRead nr WHERE nr.childId = :childId")
    List<Long> findReadNotificationIdsByChildId(@Param("childId") Long childId);
}
