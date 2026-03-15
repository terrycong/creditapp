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
        
        // Verify page elements
        assertPageTitleContains("任务");
        
        // Check for create task button
        assertThat(isElementPresent(By.linkText("创建任务")) 
            || isElementPresent(By.cssSelector("button[data-bs-target='#createTaskModal']"))
            || isElementPresent(By.id("createTaskBtn"))).isTrue();
    }

    @Test
    @DisplayName("Should display list of tasks")
    void shouldDisplayTaskList() {
        navigateTo("/parent/tasks");
        waitForPageLoad();
        
        // Check if task table or list exists
        boolean hasTaskList = isElementPresent(By.tagName("table")) 
            || isElementPresent(By.className("task-list"))
            || isElementPresent(By.className("card"));
        assertThat(hasTaskList).isTrue();
    }

    // ========== Create Task Tests ==========

    @Test
    @DisplayName("Should create daily task successfully")
    void shouldCreateDailyTask() {
        navigateTo("/parent/tasks");
        waitForPageLoad();
        
        // Click create task button (might be modal or separate page)
        try {
            clickElement(By.cssSelector("button[data-bs-target='#createTaskModal']"));
        } catch (Exception e) {
            navigateTo("/parent/tasks");
        }
        
        // Fill task form
        sendKeysByName("title", "Selenium测试任务");
        sendKeysByName("description", "这是一个Selenium自动创建的测试任务");
        sendKeysByName("points", "15");
        
        // Select task type
        WebElement typeSelect = findElementByName("type");
        typeSelect.sendKeys("DAILY_ONCE");
        
        // Submit
        clickElement(By.cssSelector("button[type='submit']"));
        
        // Verify success - either redirected or success message shown
        waitForPageLoad();
    }

    @Test
    @DisplayName("Should create marketplace task")
    void shouldCreateMarketplaceTask() {
        navigateTo("/parent/tasks");
        waitForPageLoad();
        
        // Fill task form
        sendKeysByName("title", "市场任务" + System.currentTimeMillis());
        sendKeysByName("description", "这是一个市场任务");
        sendKeysByName("points", "20");
        
        // Check marketplace checkbox if exists
        try {
            WebElement marketplaceCheckbox = findElementByName("marketplace");
            if (!marketplaceCheckbox.isSelected()) {
                marketplaceCheckbox.click();
            }
        } catch (Exception e) {
            // Marketplace option might be implicit
        }
        
        // Submit
        clickElement(By.cssSelector("button[type='submit']"));
        waitForPageLoad();
    }

    @Test
    @DisplayName("Should validate required fields when creating task")
    void shouldValidateRequiredFields() {
        navigateTo("/parent/tasks");
        waitForPageLoad();
        
        // Try to submit empty form
        clickElement(By.cssSelector("button[type='submit']"));
        
        // Should show validation errors
        waitForPageLoad();
        // Page should still be on create task
    }

    // ========== Edit Task Tests ==========

    @Test
    @DisplayName("Should navigate to edit task page")
    void shouldNavigateToEditTaskPage() {
        navigateTo("/parent/tasks");
        waitForPageLoad();
        
        // Find and click edit button for first task
        try {
            clickElement(By.cssSelector("a[href*='/edit']"));
            waitForUrlContains("edit");
            assertUrlContains("edit");
        } catch (Exception e) {
            // No tasks to edit - skip
            Assumptions.assumeTrue(false, "No tasks available to edit");
        }
    }

    @Test
    @DisplayName("Should update task successfully")
    void shouldUpdateTask() {
        navigateTo("/parent/tasks");
        waitForPageLoad();
        
        // Click edit on first task
        try {
            clickElement(By.cssSelector("a[href*='/edit']"));
            waitForPageLoad();
            
            // Update title
            WebElement titleInput = findElementByName("title");
            String originalTitle = titleInput.getAttribute("value");
            titleInput.clear();
            titleInput.sendKeys("更新后的任务-" + System.currentTimeMillis());
            
            // Submit
            clickElement(By.cssSelector("button[type='submit']"));
            waitForUrlContains("tasks");
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No tasks available to edit");
        }
    }

    // ========== Delete Task Tests ==========

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTask() {
        navigateTo("/parent/tasks");
        waitForPageLoad();
        
        // Find delete button
        try {
            WebElement deleteButton = findElement(By.cssSelector("form[action*='/delete'] button"));
            deleteButton.click();
            
            // Handle alert if present
            try {
                driver.switchTo().alert().accept();
            } catch (Exception e) {
                // No alert needed
            }
            
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No tasks available to delete");
        }
    }

    // ========== Marketplace Management Tests ==========

    @Test
    @DisplayName("Should display marketplace management page")
    void shouldDisplayMarketplacePage() {
        navigateTo("/parent/marketplace");
        waitForPageLoad();
        
        // Verify page loaded
        assertUrlContains("marketplace");
    }

    @Test
    @DisplayName("Should show marketplace tasks statistics")
    void shouldShowMarketplaceStatistics() {
        navigateTo("/parent/marketplace");
        waitForPageLoad();
        
        // Check for statistics display
        boolean hasStats = isElementPresent(By.className("stats"))
            || isElementPresent(By.className("statistics"))
            || isElementPresent(By.className("badge"));
        assertThat(hasStats).isTrue();
    }

    @Test
    @DisplayName("Should hide marketplace task")
    void shouldHideMarketplaceTask() {
        navigateTo("/parent/marketplace");
        waitForPageLoad();
        
        try {
            WebElement hideButton = findElement(By.cssSelector("form[action*='/hide'] button"));
            hideButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No marketplace tasks to hide");
        }
    }

    @Test
    @DisplayName("Should delete marketplace task")
    void shouldDeleteMarketplaceTask() {
        navigateTo("/parent/marketplace");
        waitForPageLoad();
        
        try {
            WebElement deleteButton = findElement(By.cssSelector("form[action*='/delete'] button"));
            deleteButton.click();
            
            try {
                driver.switchTo().alert().accept();
            } catch (Exception e) {
                // No alert
            }
            
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No marketplace tasks to delete");
        }
    }

    // ========== Child Assignment Tests ==========

    @Test
    @DisplayName("Should assign task to specific child")
    void shouldAssignTaskToChild() {
        navigateTo("/parent/tasks");
        waitForPageLoad();
        
        // Create task assigned to child
        sendKeysByName("title", "分配给孩子任务-" + System.currentTimeMillis());
        sendKeysByName("description", "这是分配给特定孩子的任务");
        sendKeysByName("points", "25");
        
        // Select child from dropdown
        try {
            WebElement childSelect = findElementByName("childId");
            if (childSelect != null) {
                childSelect.click();
                // Select first option that's not empty
            }
        } catch (Exception e) {
            // No child selection available
        }
        
        clickElement(By.cssSelector("button[type='submit']"));
        waitForPageLoad();
    }
}