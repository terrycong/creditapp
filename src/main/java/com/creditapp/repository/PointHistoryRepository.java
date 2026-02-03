package com.creditapp.repository;

import com.creditapp.entity.PointHistory;
import com.creditapp.entity.PointChangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for PointHistory entity
 */
@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    /**
     * Find all point history for a child, ordered by creation time (newest first)
     */
    List<PointHistory> findByChildIdOrderByCreatedAtDesc(Long childId);

    /**
     * Find point history by type for a child
     */
    List<PointHistory> findByChildIdAndChangeTypeOrderByCreatedAtDesc(Long childId, PointChangeType changeType);

    /**
     * Find point history within a date range
     */
    List<PointHistory> findByChildIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long childId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find recent point history (last N records)
     */
    @Query("SELECT ph FROM PointHistory ph WHERE ph.child.id = :childId ORDER BY ph.createdAt DESC")
    List<PointHistory> findRecentByChildId(@Param("childId") Long childId);

    /**
     * Count total changes by type for a child
     */
    long countByChildIdAndChangeType(Long childId, PointChangeType changeType);

    /**
     * Get total points earned by a child
     */
    @Query("SELECT COALESCE(SUM(ph.changePoints), 0) FROM PointHistory ph " +
           "WHERE ph.child.id = :childId AND ph.changePoints > 0")
    Integer getTotalPointsEarnedByChild(@Param("childId") Long childId);

    /**
     * Get total points spent by a child
     */
    @Query("SELECT COALESCE(SUM(ph.changePoints), 0) FROM PointHistory ph " +
           "WHERE ph.child.id = :childId AND ph.changePoints < 0")
    Integer getTotalPointsSpentByChild(@Param("childId") Long childId);

    /**
     * Find by reference (task completion, reward redemption, etc.)
     */
    List<PointHistory> findByReferenceIdAndReferenceType(Long referenceId, String referenceType);

    /**
     * Check if a reference has already been recorded (prevent duplicates)
     */
    boolean existsByReferenceIdAndReferenceType(Long referenceId, String referenceType);
}
