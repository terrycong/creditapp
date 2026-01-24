package com.creditapp.dto;

import lombok.Data;

@Data
public class UpdateChildRequest {
    private String username;
    private String password; // Optional - only update if provided
    private Integer points;
}