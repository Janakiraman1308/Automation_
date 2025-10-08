package org.actionMethods;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

public class PageMethods {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private Duration defaultWait = Duration.ofSeconds(10);

    public PageMethods(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, defaultWait);
    }

    // Navigation
    public void open(String url) {
        driver.get(url);
    }

    public void navigateTo(String url) {
        driver.navigate().to(url);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getTitle() {
        return driver.getTitle();
    }

    // Basic waits
    public WebElement waitForVisibility(By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForVisibility(By locator) {
        return waitForVisibility(locator, defaultWait);
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public boolean waitForInvisibility(By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout).until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public WebElement waitForPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // Element actions
    public void click(By locator) {
        waitForClickable(locator).click();
    }

    public void safeClick(By locator) {
        try {
            click(locator);
        } catch (WebDriverException e) {
            WebElement el = waitForVisibility(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    public boolean retryingClick(By locator, int attempts, Duration betweenAttempts) {
        for (int i = 0; i < attempts; i++) {
            try {
                click(locator);
                return true;
            } catch (WebDriverException e) {
                try {
                    Thread.sleep(betweenAttempts.toMillis());
                } catch (InterruptedException ignored) {
                }
            }
        }
        return false;
    }

    public void type(By locator, String text) {
        WebElement el = waitForVisibility(locator);
        el.clear();
        el.sendKeys(text);
    }

    public void clear(By locator) {
        waitForVisibility(locator).clear();
    }

    public void submit(By locator) {
        waitForVisibility(locator).submit();
    }

    public String getText(By locator) {
        return waitForVisibility(locator).getText();
    }

    public String getAttribute(By locator, String attribute) {
        return waitForVisibility(locator).getAttribute(attribute);
    }

    public boolean isElementPresent(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isDisplayed(By locator) {
        try {
            return waitForVisibility(locator).isDisplayed();
        } catch (WebDriverException e) {
            return false;
        }
    }

    public boolean isEnabled(By locator) {
        try {
            return waitForVisibility(locator).isEnabled();
        } catch (WebDriverException e) {
            return false;
        }
    }

    public boolean isSelected(By locator) {
        try {
            return waitForVisibility(locator).isSelected();
        } catch (WebDriverException e) {
            return false;
        }
    }

    // JS & scrolling
    public void scrollIntoView(By locator) {
        WebElement el = waitForVisibility(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
    }

    public Object executeJs(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    // Actions
    public void hover(By locator) {
        Actions actions = new Actions(driver);
        actions.moveToElement(waitForVisibility(locator)).perform();
    }

    public void doubleClick(By locator) {
        Actions actions = new Actions(driver);
        actions.doubleClick(waitForVisibility(locator)).perform();
    }

    public void rightClick(By locator) {
        Actions actions = new Actions(driver);
        actions.contextClick(waitForVisibility(locator)).perform();
    }

    public void dragAndDrop(By source, By target) {
        Actions actions = new Actions(driver);
        actions.dragAndDrop(waitForVisibility(source), waitForVisibility(target)).perform();
    }

    // Dropdowns
    public void selectByVisibleText(By locator, String text) {
        Select sel = new Select(waitForVisibility(locator));
        sel.selectByVisibleText(text);
    }

    public void selectByValue(By locator, String value) {
        Select sel = new Select(waitForVisibility(locator));
        sel.selectByValue(value);
    }

    public void selectByIndex(By locator, int index) {
        Select sel = new Select(waitForVisibility(locator));
        sel.selectByIndex(index);
    }

    // Frames
    public void switchToFrame(By locator) {
        WebElement frame = waitForPresence(locator);
        driver.switchTo().frame(frame);
    }

    public void switchToFrame(int index) {
        driver.switchTo().frame(index);
    }

    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    // Alerts
    public void acceptAlert() {
        wait.until(ExpectedConditions.alertIsPresent()).accept();
    }

    public void dismissAlert() {
        wait.until(ExpectedConditions.alertIsPresent()).dismiss();
    }

    public String getAlertText() {
        return wait.until(ExpectedConditions.alertIsPresent()).getText();
    }

    // Windows
    public boolean switchToWindowByTitle(String title, Duration timeout) {
        long end = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < end) {
            Set<String> handles = driver.getWindowHandles();
            for (String h : handles) {
                driver.switchTo().window(h);
                if (driver.getTitle().equals(title)) return true;
            }
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    public Set<String> getWindowHandles() {
        return driver.getWindowHandles();
    }

    // Page load wait
    public boolean waitForPageLoad(Duration timeout) {
        FluentWait<WebDriver> fw = new FluentWait<>(driver).withTimeout(timeout).pollingEvery(Duration.ofMillis(200));
        return fw.until((Function<WebDriver, Boolean>) drv -> ((JavascriptExecutor) drv)
                .executeScript("return document.readyState").equals("complete"));
    }

    // Screenshot
    public void takeScreenshot(String outputPath) {
        if (!(driver instanceof TakesScreenshot)) return;
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            File dest = new File(outputPath);
            Files.createDirectories(dest.getParentFile().toPath());
            Files.copy(src.toPath(), dest.toPath());
        } catch (IOException ignored) {
        }
    }

    // Utilities
    public void close() {
        driver.close();
    }

    public void quit() {
        driver.quit();
    }

    // Fluent wait example for custom conditions
    public <T> T fluentWait(Function<WebDriver, T> condition, Duration timeout, Duration poll) {
        return new FluentWait<>(driver).withTimeout(timeout).pollingEvery(poll).until(condition);
    }
}