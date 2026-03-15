package com.creditapp.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium Tests for Login and Authentication
 * Tests login page, registration, and session management
 */
@DisplayName("Login & Authentication UI Tests")
class LoginSeleniumTest extends BaseSeleniumTest {

    // ========== Login Page Tests ==========

    @Test
    @DisplayName("Should display login page with correct elements")
    void shouldDisplayLoginPage() {
        navigateTo("/login");
        
        // Verify page title
        assertPageTitleContains("登录");
        
        // Verify form elements exist
        assertThat(isElementPresent(By.name("username"))).isTrue();
        assertThat(isElementPresent(By.name("password"))).isTrue();
        assertThat(isElementPresent(By.cssSelector("button[type='submit']"))).isTrue();
        
        // Verify registration link
        assertThat(isElementPresent(By.linkText("注册"))).isTrue();
    }

    @Test
    @DisplayName("Should login successfully as parent")
    void shouldLoginAsParentSuccessfully() {
        navigateTo("/login");
        
        // Fill login form
        sendKeysByName("username", "parent");
        sendKeysByName("password", "parent123");
        
        // Submit form
        clickElement(By.cssSelector("button[type='submit']"));
        
        // Verify redirect to dashboard
        waitForUrlContains("dashboard");
        assertUrlContains("dashboard");
    }

    @Test
    @DisplayName("Should login successfully as child")
    void shouldLoginAsChildSuccessfully() {
        navigateTo("/login");
        
        // Fill login form
        sendKeysByName("username", "child");
        sendKeysByName("password", "child123");
        
        // Submit form
        clickElement(By.cssSelector("button[type='submit']"));
        
        // Verify redirect to dashboard
        waitForUrlContains("dashboard");
        assertUrlContains("dashboard");
    }

    @Test
    @DisplayName("Should show error for invalid credentials")
    void shouldShowErrorForInvalidCredentials() {
        navigateTo("/login");
        
        // Fill login form with wrong credentials
        sendKeysByName("username", "wronguser");
        sendKeysByName("password", "wrongpass");
        
        // Submit form
        clickElement(By.cssSelector("button[type='submit']"));
        
        // Should stay on login page or show error
        // This depends on the actual error handling implementation
        waitForPageLoad();
        
        // Verify error message or still on login page
        boolean hasError = isElementPresent(By.className("alert-danger")) 
            || getCurrentUrl().contains("error")
            || getCurrentUrl().contains("login");
        assertThat(hasError).isTrue();
    }

    @Test
    @DisplayName("Should show error for empty username")
    void shouldShowErrorForEmptyUsername() {
        navigateTo("/login");
        
        // Leave username empty
        sendKeysByName("username", "");
        sendKeysByName("password", "somepassword");
        
        // Submit form
        clickElement(By.cssSelector("button[type='submit']"));
        
        // Should show validation error or stay on page
        waitForPageLoad();
        assertUrlContains("login");
    }

    @Test
    @DisplayName("Should show error for empty password")
    void shouldShowErrorForEmptyPassword() {
        navigateTo("/login");
        
        // Leave password empty
        sendKeysByName("username", "parent");
        sendKeysByName("password", "");
        
        // Submit form
        clickElement(By.cssSelector("button[type='submit']"));
        
        // Should show validation error or stay on page
        waitForPageLoad();
        assertUrlContains("login");
    }

    // ========== Registration Tests ==========

    @Test
    @DisplayName("Should navigate to registration page")
    void shouldNavigateToRegistrationPage() {
        navigateTo("/login");
        
        // Click registration link
        clickElement(By.linkText("注册"));
        
        // Verify on registration page
        waitForUrlContains("register");
        assertUrlContains("register");
    }

    @Test
    @DisplayName("Should display registration form")
    void shouldDisplayRegistrationForm() {
        navigateTo("/register");
        
        // Verify form elements
        assertThat(isElementPresent(By.name("username"))).isTrue();
        assertThat(isElementPresent(By.name("password"))).isTrue();
        assertThat(isElementPresent(By.cssSelector("button[type='submit']"))).isTrue();
    }

    @Test
    @DisplayName("Should register new parent successfully")
    void shouldRegisterNewParent() {
        navigateTo("/register");
        
        // Fill registration form
        String uniqueUsername = "testparent" + System.currentTimeMillis();
        sendKeysByName("username", uniqueUsername);
        sendKeysByName("password", "testpass123");
        
        // Submit form
        clickElement(By.cssSelector("button[type='submit']"));
        
        // Should redirect to login page with success message
        waitForUrlContains("login");
    }

    // ========== Session Tests ==========

    @Test
    @DisplayName("Should redirect to login when accessing protected page without auth")
    void shouldRedirectToLoginForProtectedPage() {
        // Try to access dashboard without login
        navigateTo("/dashboard");
        
        // Should redirect to login
        waitForUrlContains("login");
        assertUrlContains("login");
    }

    @Test
    @DisplayName("Should access dashboard after login")
    void shouldAccessDashboardAfterLogin() {
        // Login first
        loginAsParent();
        
        // Navigate to dashboard
        navigateTo("/dashboard");
        
        // Should be able to access dashboard
        assertUrlContains("dashboard");
    }

    @Test
    @DisplayName("Should logout successfully")
    void shouldLogoutSuccessfully() {
        // Login first
        loginAsParent();
        
        // Verify logged in
        assertUrlContains("dashboard");
        
        // Logout
        logout();
        
        // Try to access protected page
        navigateTo("/dashboard");
        
        // Should redirect to login
        waitForUrlContains("login");
    }

    // ========== Role-based Access Tests ==========

    @Test
    @DisplayName("Parent should see parent-specific navigation")
    void parentShouldSeeParentNavigation() {
        loginAsParent();
        
        // Verify parent navigation elements
        assertThat(isElementPresent(By.linkText("任务管理"))).isTrue();
        assertThat(isElementPresent(By.linkText("孩子管理"))).isTrue();
    }

    @Test
    @DisplayName("Child should see child-specific navigation")
    void childShouldSeeChildNavigation() {
        loginAsChild();
        
        // Verify child navigation elements
        assertThat(isElementPresent(By.linkText("我的任务"))).isTrue();
        assertThat(isElementPresent(By.linkText("任务市场"))).isTrue();
    }
}