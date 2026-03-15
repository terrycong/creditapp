package com.creditapp.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;

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
        
        assertThat(getCurrentUrl().contains("children")).isTrue();
    }

    @Test
    @DisplayName("Should display create child page")
    void shouldDisplayCreateChildPage() {
        navigateTo("/parent/create-child");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("create-child")).isTrue();
        
        // Check for form elements
        assertThat(isElementPresent(By.name("username"))).isTrue();
        assertThat(isElementPresent(By.name("password"))).isTrue();
    }

    // ========== Approval Tests ==========

    @Test
    @DisplayName("Should display approvals page")
    void shouldDisplayApprovalsPage() {
        navigateTo("/parent/approvals");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("approvals")).isTrue();
    }

    // ========== Draft Approval Tests ==========

    @Test
    @DisplayName("Should display drafts approval page")
    void shouldDisplayDraftsPage() {
        navigateTo("/parent/drafts");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("drafts")).isTrue();
    }

    // ========== Notification Tests ==========

    @Test
    @DisplayName("Should display notifications page")
    void shouldDisplayNotificationsPage() {
        navigateTo("/parent/notifications");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("notifications")).isTrue();
    }

    // ========== Lottery Management Tests ==========

    @Test
    @DisplayName("Should display lottery management page")
    void shouldDisplayLotteryPage() {
        navigateTo("/parent/lottery");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("lottery")).isTrue();
    }

    // ========== Dashboard Tests ==========

    @Test
    @DisplayName("Should display parent dashboard")
    void shouldDisplayDashboard() {
        navigateTo("/dashboard");
        waitForPageLoad();
        
        assertThat(getCurrentUrl().contains("dashboard")).isTrue();
    }
}