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
 * 
 * Note: Tests may need longer timeouts in CI/CD environments
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public abstract class BaseSeleniumTest {

    protected static WebDriver driver;
    protected static WebDriverWait wait;
    
    @Value("${server.port:8080}")
    protected int serverPort;
    
    protected String baseUrl;

    // Longer timeout for slower environments
    private static final int DEFAULT_TIMEOUT_SECONDS = 15;

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
        options.addArguments("--disable-software-rasterizer");
        options.addArguments("--disable-web-security");
        options.addArguments("--remote-allow-origins=*");
        
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
        baseUrl = "http://localhost:" + serverPort;
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                // Ignore quit errors
            }
        }
    }

    // ========== Utility Methods ==========

    protected void navigateTo(String path) {
        String url = baseUrl + path;
        driver.get(url);
        // Wait for page to be in ready state
        wait.until(webDriver -> 
            ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState")
                .equals("complete"));
        // Small delay for dynamic content
        try { Thread.sleep(500); } catch (InterruptedException e) {}
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
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            // Try JavaScript click as fallback
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
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
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            driver.findElement(by);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            return true;
        } catch (NoSuchElementException e) {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
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

    protected void sleep(int milliseconds) {
        try { Thread.sleep(milliseconds); } catch (InterruptedException e) {}
    }

    // ========== Login Helpers ==========

    protected void loginAsParent() {
        navigateTo("/login");
        waitForPageLoad();
        
        // Wait for form elements
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        
        sendKeysByName("username", "parent");
        sendKeysByName("password", "parent123");
        
        // Submit form
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[type='submit']")));
        submitBtn.click();
        
        // Wait for redirect to dashboard
        sleep(1000); // Give time for authentication
        waitForUrlContains("dashboard");
    }

    protected void loginAsChild() {
        navigateTo("/login");
        waitForPageLoad();
        
        // Wait for form elements
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        
        sendKeysByName("username", "child");
        sendKeysByName("password", "child123");
        
        // Submit form
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[type='submit']")));
        submitBtn.click();
        
        // Wait for redirect to dashboard
        sleep(1000); // Give time for authentication
        waitForUrlContains("dashboard");
    }

    protected void logout() {
        try {
            navigateTo("/login");
        } catch (Exception e) {
            driver.get(baseUrl + "/login");
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