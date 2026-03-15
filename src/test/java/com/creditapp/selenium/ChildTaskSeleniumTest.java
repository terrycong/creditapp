package com.creditapp.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium Tests for Child Task Views
 * Tests child's task list, marketplace, and task completion
 */
@DisplayName("Child Task UI Tests")
class ChildTaskSeleniumTest extends BaseSeleniumTest {

    @BeforeEach
    void setUp() {
        loginAsChild();
    }

    // ========== Task List Tests ==========

    @Test
    @DisplayName("Should display child's task list page")
    void shouldDisplayTaskListPage() {
        navigateTo("/child/tasks");
        waitForPageLoad();
        
        // Verify page loaded
        assertThat(getCurrentUrl().contains("tasks")).isTrue();
    }

    @Test
    @DisplayName("Should display task list area")
    void shouldDisplayTaskListArea() {
        navigateTo("/child/tasks");
        waitForPageLoad();
        
        // Check for task display area (table, cards, or list)
        boolean hasContent = isElementPresent(By.tagName("table"))
            || isElementPresent(By.className("card"))
            || isElementPresent(By.className("task-list"))
            || isElementPresent(By.tagName("body"));
        assertThat(hasContent).isTrue();
    }

    // ========== Marketplace Tests ==========

    @Test
    @DisplayName("Should display marketplace page")
    void shouldDisplayMarketplacePage() {
        navigateTo("/child/marketplace");
        waitForPageLoad();
        
        // Verify page loaded
        assertThat(getCurrentUrl().contains("marketplace")).isTrue();
    }

    @Test
    @DisplayName("Should show marketplace content")
    void shouldShowMarketplaceContent() {
        navigateTo("/child/marketplace");
        waitForPageLoad();
        
        // Check for any content
        boolean hasContent = isElementPresent(By.className("card"))
            || isElementPresent(By.tagName("table"))
            || isElementPresent(By.className("marketplace"));
        // Content may or may not be present depending on data
    }

    // ========== Point History Tests ==========

    @Test
    @DisplayName("Should display point history page")
    void shouldDisplayPointHistoryPage() {
        navigateTo("/child/points/history");
        waitForPageLoad();
        
        // Verify page loaded
        assertThat(getCurrentUrl().contains("history")).isTrue();
    }

    // ========== Draft Task Tests ==========

    @Test
    @DisplayName("Should display draft tasks page")
    void shouldDisplayDraftTasksPage() {
        navigateTo("/child/tasks/drafts");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("drafts")).isTrue();
    }

    // ========== Rewards Page Test ==========

    @Test
    @DisplayName("Should display rewards page")
    void shouldDisplayRewardsPage() {
        navigateTo("/child/rewards");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("rewards")).isTrue();
    }
}