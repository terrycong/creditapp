package com.creditapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for point expiration settings
 */
@Component
@ConfigurationProperties(prefix = "points.expiration")
public class PointExpirationProperties {

    /**
     * Enable/disable point expiration (default: true)
     */
    private boolean enabled = true;

    /**
     * Expiration period in days (default: 180 days)
     */
    private int expirationDays = 180;

    /**
     * Check for expired points daily (cron expression)
     */
    private String checkCron = "0 0 2 * * ?"; // Daily at 2 AM

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getExpirationDays() {
        return expirationDays;
    }

    public void setExpirationDays(int expirationDays) {
        this.expirationDays = expirationDays;
    }

    public String getCheckCron() {
        return checkCron;
    }

    public void setCheckCron(String checkCron) {
        this.checkCron = checkCron;
    }
}
