package com.creditapp.service;

import com.creditapp.dto.DashboardStatsDTO;

public interface DashboardService {
    /**
     * Get dashboard statistics for a parent user
     * @param parentId The ID of the parent user
     * @return Dashboard statistics including recent activity, child statistics, etc.
     */
    DashboardStatsDTO getParentDashboardStats(Long parentId);
    
    /**
     * Get dashboard statistics for a child user
     * @param childId The ID of the child user
     * @return Simplified dashboard statistics for child view
     */
    DashboardStatsDTO getChildDashboardStats(Long childId);
}