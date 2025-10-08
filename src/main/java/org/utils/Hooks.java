package org.utils;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;

public class Hooks {

    @BeforeAll
    public static void beforeAll() {
        String browser = System.getProperty("browser", System.getenv().getOrDefault("BROWSER", "chrome"));
        DriverFactory.initDriver(browser);
    }

    @AfterAll
    public static void afterAll() {
        DriverFactory.quitDriver();
    }
}