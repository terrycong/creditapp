package com.creditapp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PenaltyRecordDTO {
    private Long id;
    private Long childId;
    private String childName;
    private Long penaltyRuleId;
    private String penaltyRuleName;
    private Integer points;
    private String note;
    private String appliedByName;
    private LocalDateTime appliedAt;
}
