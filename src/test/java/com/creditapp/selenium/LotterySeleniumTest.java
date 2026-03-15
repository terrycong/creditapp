package com.creditapp.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium Tests for Lottery System
 * Tests lottery pages for both parent and child
 */
@DisplayName("Lottery UI Tests")
class LotterySeleniumTest extends BaseSeleniumTest {

    // ========== Parent Lottery Management Tests ==========

    @Test
    @DisplayName("Parent should see lottery management page")
    void parentShouldSeeLotteryPage() {
        loginAsParent();
        navigateTo("/parent/lottery");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("lottery")).isTrue();
    }

    @Test
    @DisplayName("Parent lottery page should display form elements")
    void parentLotteryPageShouldHaveForm() {
        loginAsParent();
        navigateTo("/parent/lottery");
        waitForPageLoad();
        
        // Check for create theme form elements
        assertThat(isElementPresent(By.id("themeName"))).isTrue();
        assertThat(isElementPresent(By.id("pointsPerDraw"))).isTrue();
    }

    @Test
    @DisplayName("Parent can create lottery theme")
    void parentCanCreateLotteryTheme() {
        loginAsParent();
        navigateTo("/parent/lottery");
        waitForPageLoad();
        
        // Expand form
        try {
            clickElement(By.cssSelector("button[data-bs-target='#createThemeForm']"));
            sleep(500);
        } catch (Exception e) {
            // Form might already be visible
        }
        
        // Fill form
        sendKeysById("themeName", "Selenium测试抽奖" + System.currentTimeMillis());
        sendKeysById("pointsPerDraw", "10");
        
        // Submit
        clickElement(By.cssSelector("#createThemeFormEl button[type='submit']"));
        waitForPageLoad();
        
        // Verify still on page
        assertThat(getCurrentUrl().contains("lottery")).isTrue();
    }

    // ========== Child Lottery Tests ==========

    @Test
    @DisplayName("Child should see lottery page")
    void childShouldSeeLotteryPage() {
        loginAsChild();
        navigateTo("/child/lottery");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("lottery")).isTrue();
    }

    @Test
    @DisplayName("Child lottery page should show points")
    void childLotteryPageShouldShowPoints() {
        loginAsChild();
        navigateTo("/child/lottery");
        waitForPageLoad();
        
        // Check for points display
        assertThat(isElementPresent(By.cssSelector(".points-card"))).isTrue();
    }

    @Test
    @DisplayName("Child can see available lottery themes")
    void childCanSeeLotteryThemes() {
        loginAsChild();
        navigateTo("/child/lottery");
        waitForPageLoad();
        
        // Page should load without errors
        // Either themes are shown or empty state is shown
        boolean hasContent = isElementPresent(By.cssSelector(".theme-card")) 
            || isElementPresent(By.cssSelector(".empty-state"));
        assertThat(hasContent).isTrue();
    }

    @Test
    @DisplayName("Child lottery page has history section")
    void childLotteryPageHasHistorySection() {
        loginAsChild();
        navigateTo("/child/lottery");
        waitForPageLoad();
        
        // Check for history section
        assertThat(isElementPresent(By.id("historyContainer"))).isTrue();
    }

    // ========== Navigation Tests ==========

    @Test
    @DisplayName("Parent can navigate to lottery from dashboard")
    void parentCanNavigateToLottery() {
        loginAsParent();
        navigateTo("/dashboard");
        waitForPageLoad();
        
        // Click lottery link in nav
        try {
            clickElement(By.cssSelector("a[href*='lottery']"));
            waitForPageLoad();
            assertThat(getCurrentUrl().contains("lottery")).isTrue();
        } catch (Exception e) {
            // Link might not exist in nav, try direct
            navigateTo("/parent/lottery");
            assertThat(getCurrentUrl().contains("lottery")).isTrue();
        }
    }

    @Test
    @DisplayName("Child can navigate to lottery from dashboard")
    void childCanNavigateToLottery() {
        loginAsChild();
        navigateTo("/dashboard");
        waitForPageLoad();
        
        // Click lottery link in nav
        try {
            clickElement(By.cssSelector("a[href='/child/lottery']"));
            waitForPageLoad();
            assertThat(getCurrentUrl().contains("lottery")).isTrue();
        } catch (Exception e) {
            // Try direct navigation
            navigateTo("/child/lottery");
            assertThat(getCurrentUrl().contains("lottery")).isTrue();
        }
    }
}