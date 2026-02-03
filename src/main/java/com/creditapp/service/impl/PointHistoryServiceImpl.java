package com.creditapp.service.impl;

import com.creditapp.dto.PointHistoryDTO;
import com.creditapp.entity.Child;
import com.creditapp.entity.PointChangeType;
import com.creditapp.entity.PointHistory;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.repository.ChildRepository;
import com.creditapp.repository.PointHistoryRepository;
import com.creditapp.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing point history records
 */
@Service
@RequiredArgsConstructor
public class PointHistoryServiceImpl implements PointHistoryService {

    private static final Logger log = LoggerFactory.getLogger(PointHistoryServiceImpl.class);

    private final PointHistoryRepository pointHistoryRepository;
    private final ChildRepository childRepository;

    @Override
    @Transactional
    public PointHistory recordPointChange(Long childId, Integer changePoints,
                                          PointChangeType changeType, String description,
                                          Long referenceId, String referenceType,
                                          Long changedById) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", childId));

        // Check for duplicate reference
        if (referenceId != null && referenceType != null) {
            if (pointHistoryRepository.existsByReferenceIdAndReferenceType(referenceId, referenceType)) {
                log.warn("Duplicate point history record detected for reference: {} id: {}",
                        referenceType, referenceId);
                return null;
            }
        }

        int originalPoints = child.getPoints();
        int afterPoints = originalPoints + changePoints;

        PointHistory history = PointHistory.builder()
                .child(child)
                .originalPoints(originalPoints)
                .changePoints(changePoints)
                .afterPoints(afterPoints)
                .changeType(changeType)
                .description(description)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .changedById(changedById)
                .createdAt(LocalDateTime.now())
                .build();

        PointHistory saved = pointHistoryRepository.save(history);
        log.info("Recorded point change for child {}: {} {} ({} -> {})",
                childId, changePoints, changeType.getDisplayName(), originalPoints, afterPoints);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointHistoryDTO> getPointHistoryByChildId(Long childId) {
        List<PointHistory> historyList = pointHistoryRepository.findByChildIdOrderByCreatedAtDesc(childId);
        return historyList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointHistoryDTO> getPointHistoryByType(Long childId, PointChangeType changeType) {
        List<PointHistory> historyList = pointHistoryRepository
                .findByChildIdAndChangeTypeOrderByCreatedAtDesc(childId, changeType);
        return historyList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointHistoryDTO> getPointHistoryByDateRange(Long childId,
                                                             LocalDateTime startDate,
                                                             LocalDateTime endDate) {
        List<PointHistory> historyList = pointHistoryRepository
                .findByChildIdAndCreatedAtBetweenOrderByCreatedAtDesc(childId, startDate, endDate);
        return historyList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PointHistoryDTO getLatestRecord(Long childId) {
        List<PointHistory> historyList = pointHistoryRepository.findByChildIdOrderByCreatedAtDesc(childId);
        if (historyList.isEmpty()) {
            return null;
        }
        return toDTO(historyList.get(0));
    }

    private PointHistoryDTO toDTO(PointHistory history) {
        return PointHistoryDTO.builder()
                .id(history.getId())
                .childId(history.getChild().getId())
                .originalPoints(history.getOriginalPoints())
                .changePoints(history.getChangePoints())
                .afterPoints(history.getAfterPoints())
                .changeType(history.getChangeType())
                .changeTypeName(history.getChangeType().getDisplayName())
                .description(history.getDescription())
                .referenceId(history.getReferenceId())
                .referenceType(history.getReferenceType())
                .createdAt(history.getCreatedAt())
                .formattedChange(history.getFormattedChange())
                .isEarning(history.isEarning())
                .isSpending(history.isSpending())
                .build();
    }
}
