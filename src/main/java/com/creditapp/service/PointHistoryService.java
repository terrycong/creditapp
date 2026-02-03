package com.creditapp.service;

import com.creditapp.dto.PointHistoryDTO;
import com.creditapp.entity.PointChangeType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for PointHistory operations
 */
public interface PointHistoryService {

    /**
     * Record a point change for a child
     *
     * @param childId The child whose points changed
     * @param changePoints The change amount (positive for earning, negative for spending)
     * @param changeType The type of change
     * @param description Optional description
     * @param referenceId Optional reference ID (e.g., task completion ID)
     * @param referenceType Optional reference type (e.g., "TASK_COMPLETION")
     * @param changedById ID of who made the change
     * @return The created PointHistory record
     */
    com.creditapp.entity.PointHistory recordPointChange(Long childId, Integer changePoints,
                                                       PointChangeType changeType, String description,
                                                       Long referenceId, String referenceType,
                                                       Long changedById);

    /**
     * Get all point history for a child
     */
    List<PointHistoryDTO> getPointHistoryByChildId(Long childId);

    /**
     * Get point history by change type
     */
    List<PointHistoryDTO> getPointHistoryByType(Long childId, PointChangeType changeType);

    /**
     * Get point history within a date range
     */
    List<PointHistoryDTO> getPointHistoryByDateRange(Long childId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get the latest point history record for a child
     */
    PointHistoryDTO getLatestRecord(Long childId);
}
