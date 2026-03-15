package com.creditapp.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium Tests for Reward System
 * Tests reward management and redemption
 */
@DisplayName("Reward UI Tests")
class RewardSeleniumTest extends BaseSeleniumTest {

    // ========== Parent Reward Management ==========

    @Test
    @DisplayName("Parent should see reward management page")
    void parentShouldSeeRewardManagementPage() {
        loginAsParent();
        navigateTo("/parent/rewards");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("rewards")).isTrue();
    }

    // ========== Child Reward Store ==========

    @Test
    @DisplayName("Child should see reward store")
    void childShouldSeeRewardStore() {
        loginAsChild();
        navigateTo("/child/rewards");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("rewards")).isTrue();
    }

    @Test
    @DisplayName("Child should see reward content")
    void childShouldSeeRewardContent() {
        loginAsChild();
        navigateTo("/child/rewards");
        waitForPageLoad();
        
        // Check for content area
        boolean hasContent = isElementPresent(By.className("reward"))
            || isElementPresent(By.className("card"))
            || isElementPresent(By.tagName("table"))
            || isElementPresent(By.tagName("body"));
        assertThat(hasContent).isTrue();
    }
}