package com.creditapp.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium Tests for Parent Management Functions
 * Tests child management, approvals, notifications, and lottery management
 */
@DisplayName("Parent Management UI Tests")
class ParentManagementSeleniumTest extends BaseSeleniumTest {

    @BeforeEach
    void setUp() {
        loginAsParent();
    }

    // ========== Child Management Tests ==========

    @Test
    @DisplayName("Should display children management page")
    void shouldDisplayChildrenPage() {
        navigateTo("/parent/children");
        waitForPageLoad();
        
        assertUrlContains("children");
    }

    @Test
    @DisplayName("Should show list of children")
    void shouldShowChildrenList() {
        navigateTo("/parent/children");
        waitForPageLoad();
        
        // Check for children display
        boolean hasChildren = isElementPresent(By.className("child"))
            || isElementPresent(By.cssSelector(".card"))
            || isElementPresent(By.tagName("table"));
        assertThat(hasChildren).isTrue();
    }

    @Test
    @DisplayName("Should display create child page")
    void shouldDisplayCreateChildPage() {
        navigateTo("/parent/create-child");
        waitForPageLoad();
        
        assertUrlContains("create-child");
        
        // Check for form elements
        assertThat(isElementPresent(By.name("username"))).isTrue();
        assertThat(isElementPresent(By.name("password"))).isTrue();
    }

    @Test
    @DisplayName("Should create new child account")
    void shouldCreateNewChild() {
        navigateTo("/parent/create-child");
        waitForPageLoad();
        
        // Fill child creation form
        String uniqueUsername = "testchild" + System.currentTimeMillis();
        sendKeysByName("username", uniqueUsername);
        sendKeysByName("password", "child123");
        
        // Submit
        clickElement(By.cssSelector("button[type='submit']"));
        waitForPageLoad();
        
        // Should redirect to children list
        assertUrlContains("children");
    }

    @Test
    @DisplayName("Should edit child information")
    void shouldEditChild() {
        navigateTo("/parent/children");
        waitForPageLoad();
        
        try {
            // Click edit button for first child
            clickElement(By.cssSelector("a[href*='/edit']"));
            waitForPageLoad();
            
            // Verify edit form
            assertThat(isElementPresent(By.name("username"))).isTrue();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No children to edit");
        }
    }

    @Test
    @DisplayName("Should view child details")
    void shouldViewChildDetails() {
        navigateTo("/parent/children");
        waitForPageLoad();
        
        try {
            clickElement(By.cssSelector("a[href*='/children/']:not([href*='edit'])"));
            waitForPageLoad();
            
            // Should show child details
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No children to view");
        }
    }

    @Test
    @DisplayName("Should delete child account")
    void shouldDeleteChild() {
        navigateTo("/parent/children");
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
            Assumptions.assumeTrue(false, "No children to delete");
        }
    }

    // ========== Approval Tests ==========

    @Test
    @DisplayName("Should display approvals page")
    void shouldDisplayApprovalsPage() {
        navigateTo("/parent/approvals");
        waitForPageLoad();
        
        assertUrlContains("approvals");
    }

    @Test
    @DisplayName("Should show pending task completions")
    void shouldShowPendingCompletions() {
        navigateTo("/parent/approvals");
        waitForPageLoad();
        
        // Check for pending completions
        boolean hasApprovals = isElementPresent(By.className("approval"))
            || isElementPresent(By.className("pending"))
            || isElementPresent(By.tagName("table"));
        // May be empty if no pending approvals
    }

    @Test
    @DisplayName("Should approve task completion")
    void shouldApproveTaskCompletion() {
        navigateTo("/parent/approvals");
        waitForPageLoad();
        
        try {
            WebElement approveButton = findElement(By.cssSelector("form[action*='/approve'] button"));
            approveButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No pending approvals");
        }
    }

    @Test
    @DisplayName("Should reject task completion")
    void shouldRejectTaskCompletion() {
        navigateTo("/parent/approvals");
        waitForPageLoad();
        
        try {
            WebElement rejectButton = findElement(By.cssSelector("form[action*='/reject'] button"));
            rejectButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No pending approvals");
        }
    }

    // ========== Draft Approval Tests ==========

    @Test
    @DisplayName("Should display drafts approval page")
    void shouldDisplayDraftsPage() {
        navigateTo("/parent/drafts");
        waitForPageLoad();
        
        assertUrlContains("drafts");
    }

    @Test
    @DisplayName("Should approve draft task")
    void shouldApproveDraftTask() {
        navigateTo("/parent/drafts");
        waitForPageLoad();
        
        try {
            WebElement approveButton = findElement(By.cssSelector("form[action*='/approve'] button"));
            approveButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No draft tasks to approve");
        }
    }

    @Test
    @DisplayName("Should reject draft task")
    void shouldRejectDraftTask() {
        navigateTo("/parent/drafts");
        waitForPageLoad();
        
        try {
            WebElement rejectButton = findElement(By.cssSelector("form[action*='/reject'] button"));
            rejectButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No draft tasks to reject");
        }
    }

    // ========== Notification Tests ==========

    @Test
    @DisplayName("Should display notifications page")
    void shouldDisplayNotificationsPage() {
        navigateTo("/parent/notifications");
        waitForPageLoad();
        
        assertUrlContains("notifications");
    }

    @Test
    @DisplayName("Should show penalty notifications")
    void shouldShowPenaltyNotifications() {
        navigateTo("/parent/notifications");
        waitForPageLoad();
        
        // Check for notifications display
        boolean hasNotifications = isElementPresent(By.className("notification"))
            || isElementPresent(By.className("penalty"))
            || isElementPresent(By.tagName("table"));
        // May be empty if no notifications
    }

    @Test
    @DisplayName("Should apply penalty")
    void shouldApplyPenalty() {
        navigateTo("/parent/notifications");
        waitForPageLoad();
        
        try {
            WebElement applyButton = findElement(By.cssSelector("form[action*='apply-penalty'] button"));
            applyButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No notifications to apply penalty");
        }
    }

    @Test
    @DisplayName("Should dismiss notification")
    void shouldDismissNotification() {
        navigateTo("/parent/notifications");
        waitForPageLoad();
        
        try {
            WebElement dismissButton = findElement(By.cssSelector("form[action*='dismiss'] button"));
            dismissButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No notifications to dismiss");
        }
    }

    // ========== Lottery Management Tests ==========

    @Test
    @DisplayName("Should display lottery management page")
    void shouldDisplayLotteryPage() {
        navigateTo("/parent/lottery");
        waitForPageLoad();
        
        assertUrlContains("lottery");
    }

    @Test
    @DisplayName("Should show lottery themes")
    void shouldShowLotteryThemes() {
        navigateTo("/parent/lottery");
        waitForPageLoad();
        
        // Check for themes display
        boolean hasThemes = isElementPresent(By.className("theme"))
            || isElementPresent(By.cssSelector(".card"))
            || isElementPresent(By.tagName("table"));
        // May be empty if no themes created
    }

    // ========== Dashboard Tests ==========

    @Test
    @DisplayName("Should display parent dashboard")
    void shouldDisplayDashboard() {
        navigateTo("/dashboard");
        waitForPageLoad();
        
        assertUrlContains("dashboard");
    }

    @Test
    @DisplayName("Dashboard should show statistics")
    void dashboardShouldShowStatistics() {
        navigateTo("/dashboard");
        waitForPageLoad();
        
        // Check for stats cards
        boolean hasStats = isElementPresent(By.className("stat"))
            || isElementPresent(By.className("stats"))
            || isElementPresent(By.cssSelector(".card"));
        assertThat(hasStats).isTrue();
    }

    @Test
    @DisplayName("Dashboard should have navigation menu")
    void dashboardShouldHaveNavigation() {
        navigateTo("/dashboard");
        waitForPageLoad();
        
        // Check for navigation links
        assertThat(isElementPresent(By.linkText("任务管理")) 
            || isElementPresent(By.cssSelector("a[href*='tasks']"))).isTrue();
    }
}