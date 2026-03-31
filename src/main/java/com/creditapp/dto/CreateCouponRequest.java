package com.creditapp.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCouponRequest {
    
    @NotBlank(message = "Coupon code is required")
    @Size(max = 100, message = "Coupon code must be less than 100 characters")
    private String code;
    
    @NotNull(message = "Points value is required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;
    
    private Boolean enabled;
    
    @Size(max = 500, message = "Comment must be less than 500 characters")
    private String comment;
    
    @Size(max = 50, message = "Username must be less than 50 characters")
    private String username;
    
    @Min(value = 1, message = "Timeout must be at least 1 second")
    @Max(value = 86400, message = "Timeout must be less than 24 hours")
    private Integer timeoutSeconds;
}
