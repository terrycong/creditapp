package com.creditapp.service;

import com.creditapp.dto.*;
import java.util.List;

public interface PenaltyService {
    // 规则管理
    PenaltyRuleDTO createRule(CreatePenaltyRuleRequest request, Long createdById);
    List<PenaltyRuleDTO> getRulesByParent(Long parentId);
    void deleteRule(Long ruleId, Long parentId);

    // 应用扣分
    PenaltyRecordDTO applyPenalty(ApplyPenaltyRequest request, Long appliedById);
    List<PenaltyRecordDTO> getPenaltyRecordsByParent(Long parentId);
    List<PenaltyRecordDTO> getPenaltyRecordsByChild(Long childId);

    // 小孩查看规则
    List<PenaltyRuleDTO> getRulesByChild(Long childId);
}
