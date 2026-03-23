package com.creditapp.service.impl;

import com.creditapp.dto.*;
import com.creditapp.entity.*;
import com.creditapp.exception.BusinessException;
import com.creditapp.exception.ResourceNotFoundException;
import com.creditapp.repository.*;
import com.creditapp.service.PenaltyService;
import com.creditapp.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PenaltyServiceImpl implements PenaltyService {

    private final PenaltyRuleRepository penaltyRuleRepository;
    private final PenaltyRecordRepository penaltyRecordRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final PointHistoryService pointHistoryService;

    @Override
    @Transactional
    public PenaltyRuleDTO createRule(CreatePenaltyRuleRequest request, Long createdById) {
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("User", createdById));

        PenaltyRule rule = new PenaltyRule();
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setPoints(request.getPoints());
        rule.setActive(true);
        rule.setCreatedBy(createdBy);
        rule.setCreatedAt(LocalDateTime.now());

        PenaltyRule saved = penaltyRuleRepository.save(rule);
        log.info("Created penalty rule: id={}, name={}, points={}", saved.getId(), saved.getName(), saved.getPoints());

        return toRuleDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaltyRuleDTO> getRulesByParent(Long parentId) {
        return penaltyRuleRepository.findByCreatedByIdAndActiveTrue(parentId)
                .stream()
                .map(this::toRuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRule(Long ruleId, Long parentId) {
        PenaltyRule rule = penaltyRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("PenaltyRule", ruleId));

        if (!rule.getCreatedBy().getId().equals(parentId)) {
            throw new BusinessException("UNAUTHORIZED", "无权删除此规则");
        }

        rule.setActive(false);
        penaltyRuleRepository.save(rule);
        log.info("Deactivated penalty rule: id={}", ruleId);
    }

    @Override
    @Transactional
    public PenaltyRecordDTO applyPenalty(ApplyPenaltyRequest request, Long appliedById) {
        Child child = childRepository.findById(request.getChildId())
                .orElseThrow(() -> new ResourceNotFoundException("Child", request.getChildId()));

        PenaltyRule rule = penaltyRuleRepository.findById(request.getPenaltyRuleId())
                .orElseThrow(() -> new ResourceNotFoundException("PenaltyRule", request.getPenaltyRuleId()));

        User appliedBy = userRepository.findById(appliedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", appliedById));

        // 检查积分是否足够
        if (child.getPoints() < rule.getPoints()) {
            throw new BusinessException("INSUFFICIENT_POINTS", "小孩当前积分不足以扣除");
        }

        // 先记录积分变化历史（在扣除前，确保记录原始余额）
        int deductedPoints = rule.getPoints();
        pointHistoryService.recordPointChange(
                child.getId(),
                -deductedPoints, // negative because points are deducted
                PointChangeType.PENALTY,
                "违规扣分: " + rule.getName() + (request.getNote() != null ? " - " + request.getNote() : ""),
                null, // no reference ID for penalty
                "PENALTY",
                appliedById
        );
        log.info("Recorded point history for penalty: child={}, points deducted={}", child.getUsername(), deductedPoints);

        // 扣除积分
        child.setPoints(child.getPoints() - deductedPoints);
        childRepository.save(child);

        // 创建扣分记录
        PenaltyRecord record = new PenaltyRecord();
        record.setChild(child);
        record.setPenaltyRule(rule);
        record.setPoints(rule.getPoints());
        record.setNote(request.getNote());
        record.setAppliedBy(appliedBy);
        record.setAppliedAt(LocalDateTime.now());

        PenaltyRecord saved = penaltyRecordRepository.save(record);
        log.info("Applied penalty: child={}, rule={}, points={}", child.getUsername(), rule.getName(), rule.getPoints());

        return toRecordDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaltyRecordDTO> getPenaltyRecordsByParent(Long parentId) {
        return penaltyRecordRepository.findByChildParentIdOrderByAppliedAtDesc(parentId)
                .stream()
                .map(this::toRecordDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaltyRecordDTO> getPenaltyRecordsByChild(Long childId) {
        return penaltyRecordRepository.findByChildIdOrderByAppliedAtDesc(childId)
                .stream()
                .map(this::toRecordDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaltyRuleDTO> getRulesByChild(Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child", childId));
        
        // Get parent's rules (child can see what rules their parent has set)
        Long parentId = child.getParent().getId();
        return getRulesByParent(parentId);
    }

    private PenaltyRuleDTO toRuleDTO(PenaltyRule rule) {
        PenaltyRuleDTO dto = new PenaltyRuleDTO();
        dto.setId(rule.getId());
        dto.setName(rule.getName());
        dto.setDescription(rule.getDescription());
        dto.setPoints(rule.getPoints());
        dto.setActive(rule.isActive());
        dto.setCreatedAt(rule.getCreatedAt());
        return dto;
    }

    private PenaltyRecordDTO toRecordDTO(PenaltyRecord record) {
        PenaltyRecordDTO dto = new PenaltyRecordDTO();
        dto.setId(record.getId());
        dto.setChildId(record.getChild().getId());
        dto.setChildName(record.getChild().getUsername());
        dto.setPenaltyRuleId(record.getPenaltyRule().getId());
        dto.setPenaltyRuleName(record.getPenaltyRule().getName());
        dto.setPoints(record.getPoints());
        dto.setNote(record.getNote());
        dto.setAppliedByName(record.getAppliedBy().getUsername());
        dto.setAppliedAt(record.getAppliedAt());
        return dto;
    }
}
