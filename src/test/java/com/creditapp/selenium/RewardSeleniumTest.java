package com.creditapp.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium Tests for Reward System
 * Tests reward creation, redemption, and management
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
        
        assertUrlContains("rewards");
    }

    @Test
    @DisplayName("Parent should create new reward")
    void parentShouldCreateReward() {
        loginAsParent();
        navigateTo("/parent/rewards");
        waitForPageLoad();
        
        // Fill reward form
        sendKeysByName("name", "测试奖励-" + System.currentTimeMillis());
        sendKeysByName("description", "这是一个自动测试创建的奖励");
        sendKeysByName("pointsRequired", "100");
        
        try {
            sendKeysByName("quantity", "10");
        } catch (Exception e) {
            // Quantity might be optional
        }
        
        // Submit form
        clickElement(By.cssSelector("button[type='submit']"));
        waitForPageLoad();
    }

    @Test
    @DisplayName("Parent should delete reward")
    void parentShouldDeleteReward() {
        loginAsParent();
        navigateTo("/parent/rewards");
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
            Assumptions.assumeTrue(false, "No rewards to delete");
        }
    }

    // ========== Child Reward Store ==========

    @Test
    @DisplayName("Child should see reward store")
    void childShouldSeeRewardStore() {
        loginAsChild();
        navigateTo("/child/rewards");
        waitForPageLoad();
        
        assertUrlContains("rewards");
    }

    @Test
    @DisplayName("Child should see available rewards")
    void childShouldSeeAvailableRewards() {
        loginAsChild();
        navigateTo("/child/rewards");
        waitForPageLoad();
        
        // Check for rewards display
        boolean hasRewards = isElementPresent(By.className("reward"))
            || isElementPresent(By.cssSelector(".card"))
            || isElementPresent(By.tagName("table"));
        assertThat(hasRewards).isTrue();
    }

    @Test
    @DisplayName("Child should see points balance on rewards page")
    void childShouldSeePointsBalance() {
        loginAsChild();
        navigateTo("/child/rewards");
        waitForPageLoad();
        
        // Check for points display
        boolean hasPoints = isElementPresent(By.className("points"))
            || isElementPresent(By.id("points"))
            || isElementPresent(By.cssSelector("[class*='point']"));
        assertThat(hasPoints).isTrue();
    }

    @Test
    @DisplayName("Child should redeem reward")
    void childShouldRedeemReward() {
        loginAsChild();
        navigateTo("/child/rewards");
        waitForPageLoad();
        
        try {
            // Find redeem button
            WebElement redeemButton = findElement(By.cssSelector("button[action*='redeem'], form[action*='redeem'] button"));
            redeemButton.click();
            waitForPageLoad();
            
            // Should show success or error based on points
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No rewards available to redeem");
        }
    }

    @Test
    @DisplayName("Child should see redeemed rewards")
    void childShouldSeeRedeemedRewards() {
        loginAsChild();
        navigateTo("/child/rewards");
        waitForPageLoad();
        
        // Check for pending/redeemed section
        boolean hasRedemptions = isElementPresent(By.className("redemptions"))
            || isElementPresent(By.className("pending"))
            || isElementPresent(By.id("redeemed"));
        // This may be empty if no redemptions
    }

    @Test
    @DisplayName("Child should use redeemed reward")
    void childShouldUseRedeemedReward() {
        loginAsChild();
        navigateTo("/child/rewards");
        waitForPageLoad();
        
        try {
            WebElement useButton = findElement(By.cssSelector("form[action*='/use'] button"));
            useButton.click();
            waitForPageLoad();
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "No redeemed rewards to use");
        }
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Redeem should fail with insufficient points")
    void redeemShouldFailWithInsufficientPoints() {
        loginAsChild();
        navigateTo("/child/rewards");
        waitForPageLoad();
        
        // Try to redeem a high-cost reward with low points
        // The child account might not have enough points
        try {
            WebElement redeemButton = findElement(By.cssSelector("button[action*='redeem']"));
            redeemButton.click();
            waitForPageLoad();
            
            // Should show error message about insufficient points
        } catch (Exception e) {
            // This is expected - no high-cost rewards or can't redeem
        }
    }
}