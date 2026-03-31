package com.creditapp.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDTO {
    private Long id;
    private String code;
    private Integer points;
    private Boolean enabled;
    private String comment;
    private String username;
    private Integer timeoutSeconds;
    private Integer usedCount;
    private Boolean redeemed;
    private Long redeemedById;
    private String redeemedByUsername;
    private LocalDateTime redeemedAt;
    private Long createdById;
    private String createdByUsername;
    private LocalDateTime insertedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
