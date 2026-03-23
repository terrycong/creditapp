package com.creditapp.repository;

import com.creditapp.entity.PenaltyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenaltyRecordRepository extends JpaRepository<PenaltyRecord, Long> {
    List<PenaltyRecord> findByChildIdOrderByAppliedAtDesc(Long childId);
    List<PenaltyRecord> findByChildParentIdOrderByAppliedAtDesc(Long parentId);
}
