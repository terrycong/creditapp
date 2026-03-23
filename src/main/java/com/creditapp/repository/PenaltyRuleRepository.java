package com.creditapp.repository;

import com.creditapp.entity.PenaltyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenaltyRuleRepository extends JpaRepository<PenaltyRule, Long> {
    List<PenaltyRule> findByCreatedByIdAndActiveTrue(Long createdById);
    List<PenaltyRule> findByCreatedById(Long createdById);
}
