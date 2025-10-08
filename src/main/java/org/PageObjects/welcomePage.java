package org.PageObjects;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Objects;

public class welcomePage {
    private final String URL = "https://gh-users-search.netlify.app/";
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Duration defaultWait = Duration.ofSeconds(10);

    public welcomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, defaultWait);
    }

    // Navigation
    public void open() {
        driver.get(URL);
    }

    public void navigateTo(String url) {
        driver.get(url);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public boolean isAt() {
        return Objects.requireNonNull(driver.getCurrentUrl()).startsWith(URL);
    }

    // Wait helpers
    public WebElement waitForVisibility(By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForVisibility(By locator) {
        return waitForVisibility(locator, defaultWait);
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    // Element actions
    public void click(By locator) {
        waitForClickable(locator).click();
    }

    public void safeClick(By locator) {
        try {
            click(locator);
        } catch (WebDriverException e) {
            // fallback: attempt JS click
            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebElement el = waitForVisibility(locator);
            js.executeScript("arguments[0].click();", el);
        }
    }

    public void type(By locator, String text) {
        WebElement el = waitForVisibility(locator);
        el.clear();
        el.sendKeys(text);
    }

    public String getText(By locator) {
        return waitForVisibility(locator).getText();
    }

    public boolean isElementPresent(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // Utilities
    public void scrollIntoView(By locator) {
        WebElement el = waitForVisibility(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
    }

    public void hover(By locator) {
        Actions actions = new Actions(driver);
        actions.moveToElement(waitForVisibility(locator)).perform();
    }

    public void takeScreenshot(String outputPath) {
        if (!(driver instanceof TakesScreenshot)) return;
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            Files.createDirectories(new File(outputPath).getParentFile().toPath());
            Files.copy(src.toPath(), new File(outputPath).toPath());
        } catch (IOException ignored) {
        }
    }

    public void close() {
        driver.close();
    }

    public void quit() {
        driver.quit();
    }
}

