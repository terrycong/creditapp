package com.creditapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyPenaltyRequest {
    @NotNull(message = "请选择小孩")
    private Long childId;

    @NotNull(message = "请选择扣分规则")
    private Long penaltyRuleId;

    private String note;
}
