package com.creditapp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PenaltyRuleDTO {
    private Long id;
    private String name;
    private String description;
    private Integer points;
    private boolean active;
    private LocalDateTime createdAt;
}
