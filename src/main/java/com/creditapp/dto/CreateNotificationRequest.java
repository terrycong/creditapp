package com.creditapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {
    private String title;
    private String content;
    private String targetType;  // ALL or SPECIFIC_CHILD
    private Long targetChildId;
    private String priority;    // LOW, NORMAL, HIGH, URGENT
}
