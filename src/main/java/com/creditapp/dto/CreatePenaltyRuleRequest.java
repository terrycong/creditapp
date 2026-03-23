package com.creditapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePenaltyRuleRequest {
    @NotBlank(message = "规则名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "扣分分值不能为空")
    private Integer points;
}
