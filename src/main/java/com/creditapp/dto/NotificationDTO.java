package com.creditapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private Long createdById;
    private String createdByUsername;
    private String targetType;
    private Long targetChildId;
    private String priority;
    private Boolean active;
    private LocalDateTime createdAt;
    private Boolean read;  // Whether the child has read this notification
}
