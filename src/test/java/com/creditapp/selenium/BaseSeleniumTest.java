package com.creditapp.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

/**
 * Base class for Selenium UI tests
 * Provides common setup, teardown, and utility methods
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public abstract class BaseSeleniumTest {

    protected static WebDriver driver;
    protected static WebDriverWait wait;
    
    @Value("${server.port:8080}")
    protected int serverPort;
    
    protected String baseUrl;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUrl = "http://localhost:" + serverPort;
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ========== Utility Methods ==========

    protected void navigateTo(String path) {
        driver.get(baseUrl + path);
    }

    protected WebElement findElement(By by) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    protected WebElement findElementById(String id) {
        return findElement(By.id(id));
    }

    protected WebElement findElementByName(String name) {
        return findElement(By.name(name));
    }

    protected WebElement findElementByCss(String css) {
        return findElement(By.cssSelector(css));
    }

    protected WebElement findElementByXPath(String xpath) {
        return findElement(By.xpath(xpath));
    }

    protected void clickElement(By by) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(by));
        element.click();
    }

    protected void clickElementById(String id) {
        clickElement(By.id(id));
    }

    protected void sendKeys(By by, String text) {
        WebElement element = findElement(by);
        element.clear();
        element.sendKeys(text);
    }

    protected void sendKeysById(String id, String text) {
        sendKeys(By.id(id), text);
    }

    protected void sendKeysByName(String name, String text) {
        sendKeys(By.name(name), text);
    }

    protected String getPageTitle() {
        return driver.getTitle();
    }

    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    protected boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected void waitForText(By by, String text) {
        wait.until(ExpectedConditions.textToBe(by, text));
    }

    protected void waitForUrlContains(String partialUrl) {
        wait.until(ExpectedConditions.urlContains(partialUrl));
    }

    protected void waitForPageLoad() {
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState")
                .equals("complete"));
    }

    protected void takeScreenshot(String filename) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
            // Save screenshot logic can be added here
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
        }
    }

    // ========== Login Helpers ==========

    protected void loginAsParent() {
        navigateTo("/login");
        sendKeysByName("username", "parent");
        sendKeysByName("password", "parent123");
        clickElement(By.cssSelector("button[type='submit']"));
        waitForUrlContains("dashboard");
    }

    protected void loginAsChild() {
        navigateTo("/login");
        sendKeysByName("username", "child");
        sendKeysByName("password", "child123");
        clickElement(By.cssSelector("button[type='submit']"));
        waitForUrlContains("dashboard");
    }

    protected void logout() {
        try {
            clickElement(By.linkText("退出"));
        } catch (Exception e) {
            navigateTo("/login");
        }
    }

    // ========== Assertion Helpers ==========

    protected void assertPageTitleContains(String expected) {
        Assertions.assertTrue(getPageTitle().contains(expected),
            "Page title should contain: " + expected);
    }

    protected void assertElementTextContains(By by, String expected) {
        WebElement element = findElement(by);
        Assertions.assertTrue(element.getText().contains(expected),
            "Element text should contain: " + expected);
    }

    protected void assertUrlContains(String expected) {
        Assertions.assertTrue(getCurrentUrl().contains(expected),
            "URL should contain: " + expected);
    }
}