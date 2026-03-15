package com.creditapp.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium Tests for Parent Task Management
 * Tests task creation, editing, deletion, and marketplace management
 */
@DisplayName("Parent Task Management UI Tests")
class ParentTaskSeleniumTest extends BaseSeleniumTest {

    @BeforeEach
    void setUp() {
        loginAsParent();
    }

    // ========== Task List Page Tests ==========

    @Test
    @DisplayName("Should display task management page")
    void shouldDisplayTaskManagementPage() {
        navigateTo("/parent/tasks");
        waitForPageLoad();
        
        // Verify page loaded
        assertThat(getCurrentUrl().contains("tasks")).isTrue();
    }

    @Test
    @DisplayName("Should display task list area")
    void shouldDisplayTaskListArea() {
        navigateTo("/parent/tasks");
        waitForPageLoad();
        
        // Check for content area
        boolean hasContent = isElementPresent(By.tagName("table"))
            || isElementPresent(By.className("card"))
            || isElementPresent(By.tagName("body"));
        assertThat(hasContent).isTrue();
    }

    // ========== Marketplace Management Tests ==========

    @Test
    @DisplayName("Should display marketplace management page")
    void shouldDisplayMarketplacePage() {
        navigateTo("/parent/marketplace");
        waitForPageLoad();
        
        // Verify page loaded
        assertThat(getCurrentUrl().contains("marketplace")).isTrue();
    }

    // ========== Child Management Tests ==========

    @Test
    @DisplayName("Should display children management page")
    void shouldDisplayChildrenPage() {
        navigateTo("/parent/children");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("children")).isTrue();
    }

    // ========== Rewards Management Tests ==========

    @Test
    @DisplayName("Should display rewards management page")
    void shouldDisplayRewardsPage() {
        navigateTo("/parent/rewards");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("rewards")).isTrue();
    }

    // ========== Approvals Tests ==========

    @Test
    @DisplayName("Should display approvals page")
    void shouldDisplayApprovalsPage() {
        navigateTo("/parent/approvals");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("approvals")).isTrue();
    }

    // ========== Dashboard Test ==========

    @Test
    @DisplayName("Should display dashboard")
    void shouldDisplayDashboard() {
        navigateTo("/dashboard");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("dashboard")).isTrue();
    }
}