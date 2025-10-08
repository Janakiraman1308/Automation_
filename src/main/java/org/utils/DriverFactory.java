package org.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public class DriverFactory {
    private static WebDriver driver;

    public static synchronized void initDriver(String browser) {
        if (driver != null) return;

        String b = (browser == null || browser.isBlank()) ? "chrome" : browser.toLowerCase();
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", System.getenv().getOrDefault("HEADLESS", "false")));

        switch (b) {
            case "firefox":
            case "ff":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions ffOpts = new FirefoxOptions();
                if (headless) ffOpts.addArguments("--headless=new");
                driver = new FirefoxDriver(ffOpts);
                break;
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOpts = new ChromeOptions();
                if (headless) chromeOpts.addArguments("--headless=new");
                chromeOpts.addArguments("--disable-gpu", "--window-size=1920,1080");
                driver = new ChromeDriver(chromeOpts);
                break;
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        try {
            driver.manage().window().maximize();
        } catch (Exception ignored) {
            // some headless environments may not support maximize
        }
    }

    public static synchronized WebDriver getDriver() {
        if (driver == null) {
            initDriver(System.getProperty("browser", System.getenv().getOrDefault("BROWSER", "chrome")));
        }
        return driver;
    }

    public static synchronized void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
            } finally {
                driver = null;
            }
        }
    }
}