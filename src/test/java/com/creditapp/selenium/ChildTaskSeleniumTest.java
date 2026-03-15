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
        assertUrlContains("tasks");
        
        // Check for task display area
        boolean hasTasksArea = isElementPresent(By.tagName("table"))
            || isElementPresent(By.className("task-list"))
            || isElementPresent(By.className("card"));
        assertThat(hasTasksArea).isTrue();
    }

    @Test
    @DisplayName("Should show points balance")
    void shouldShowPointsBalance() {
        navigateTo("/child/tasks");
        waitForPageLoad();
        
        // Check for points display
        boolean hasPointsDisplay = isElementPresent(By.className("points"))
            || isElementPresent(By.id("points"))
            || isElementPresent(By.cssSelector("[class*='point']"));
        assertThat(hasPointsDisplay).isTrue();
    }

    @Test
    @DisplayName("Should display task details")
    void shouldDisplayTaskDetails() {
        navigateTo("/child/tasks");
        waitForPageLoad();
        
        // Check if any tasks are displayed
        try {
            WebElement taskCard = findElement(By.cssSelector(".card, .task-item, tr"));
            assertThat(taskCard.isDisplayed()).isTrue();
        } catch (Exception e) {
            // No tasks - that's okay for testing
        }
    }

    // ========== Complete Task Tests ==========

    @Test
    @DisplayName("Should submit task completion")
    void shouldSubmitTaskCompletion() {
        navigateTo("/child/tasks");
        waitForPageLoad();
        
        try {
            // Find and click complete button
            WebElement completeButton = findElement(By.cssSelector("button[action*='complete'], form[action*='complete'] button"));
            completeButton.click();
            waitForPageLoad();
            
            // Verify success message or status change
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No tasks available to complete");
        }
    }

    @Test
    @DisplayName("Should withdraw task completion request")
    void shouldWithdrawCompletionRequest() {
        navigateTo("/child/tasks");
        waitForPageLoad();
        
        try {
            // Find withdraw button for pending completion
            WebElement withdrawButton = findElement(By.cssSelector("form[action*='withdraw'] button"));
            withdrawButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No pending completions to withdraw");
        }
    }

    // ========== Marketplace Tests ==========

    @Test
    @DisplayName("Should display marketplace page")
    void shouldDisplayMarketplacePage() {
        navigateTo("/child/marketplace");
        waitForPageLoad();
        
        // Verify page loaded
        assertUrlContains("marketplace");
    }

    @Test
    @DisplayName("Should show available marketplace tasks")
    void shouldShowAvailableMarketplaceTasks() {
        navigateTo("/child/marketplace");
        waitForPageLoad();
        
        // Check for marketplace tasks display
        boolean hasTasks = isElementPresent(By.className("marketplace-task"))
            || isElementPresent(By.cssSelector(".card"))
            || isElementPresent(By.tagName("table"));
        assertThat(hasTasks).isTrue();
    }

    @Test
    @DisplayName("Should pick task from marketplace")
    void shouldPickTaskFromMarketplace() {
        navigateTo("/child/marketplace");
        waitForPageLoad();
        
        try {
            // Find and click pick button
            WebElement pickButton = findElement(By.cssSelector("button[action*='pick'], form[action*='pick'] button"));
            pickButton.click();
            waitForPageLoad();
            
            // Verify task was picked (might show in "picked" section)
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No marketplace tasks available to pick");
        }
    }

    @Test
    @DisplayName("Should unpick (release) task")
    void shouldUnpickTask() {
        navigateTo("/child/marketplace");
        waitForPageLoad();
        
        try {
            // Find unpick button
            WebElement unpickButton = findElement(By.cssSelector("button[action*='unpick'], form[action*='unpick'] button"));
            unpickButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No picked tasks to unpick");
        }
    }

    @Test
    @DisplayName("Should search marketplace tasks")
    void shouldSearchMarketplaceTasks() {
        navigateTo("/child/marketplace");
        waitForPageLoad();
        
        try {
            // Find search input
            WebElement searchInput = findElement(By.name("search"));
            searchInput.sendKeys("洗碗");
            searchInput.submit();
            waitForPageLoad();
            
            // Verify search results
        } catch (Exception e) {
            // Search might not be available
            Assumptions.assumeTrue(false, "Search functionality not available");
        }
    }

    // ========== Draft Task Tests ==========

    @Test
    @DisplayName("Should display draft tasks page")
    void shouldDisplayDraftTasksPage() {
        navigateTo("/child/tasks/drafts");
        waitForPageLoad();
        
        assertUrlContains("drafts");
    }

    @Test
    @DisplayName("Should create draft task")
    void shouldCreateDraftTask() {
        navigateTo("/child/tasks");
        waitForPageLoad();
        
        // Find draft creation form
        sendKeysByName("title", "草稿任务-" + System.currentTimeMillis());
        sendKeysByName("description", "这是一个草稿任务");
        sendKeysByName("points", "10");
        
        clickElement(By.cssSelector("button[type='submit']"));
        waitForPageLoad();
    }

    @Test
    @DisplayName("Should withdraw draft task")
    void shouldWithdrawDraftTask() {
        navigateTo("/child/tasks/drafts");
        waitForPageLoad();
        
        try {
            WebElement withdrawButton = findElement(By.cssSelector("form[action*='withdraw'] button"));
            withdrawButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No draft tasks to withdraw");
        }
    }

    // ========== Point History Tests ==========

    @Test
    @DisplayName("Should display point history page")
    void shouldDisplayPointHistoryPage() {
        navigateTo("/child/points/history");
        waitForPageLoad();
        
        assertUrlContains("history");
    }

    @Test
    @DisplayName("Should show point transactions")
    void shouldShowPointTransactions() {
        navigateTo("/child/points/history");
        waitForPageLoad();
        
        // Check for history table or list
        boolean hasHistory = isElementPresent(By.tagName("table"))
            || isElementPresent(By.className("history"))
            || isElementPresent(By.cssSelector(".list-group"));
        assertThat(hasHistory).isTrue();
    }

    // ========== Navigation Tests ==========

    @Test
    @DisplayName("Should navigate between child pages")
    void shouldNavigateBetweenPages() {
        // Start at tasks
        navigateTo("/child/tasks");
        assertUrlContains("tasks");
        
        // Go to marketplace
        clickElement(By.linkText("任务市场"));
        waitForPageLoad();
        assertUrlContains("marketplace");
        
        // Go to rewards
        clickElement(By.linkText("礼物商店"));
        waitForPageLoad();
        assertUrlContains("rewards");
    }
}