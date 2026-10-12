package ru.skambet.bdd.support;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class Hooks {

    private final Browser browser;
    private final TestData testData;

    public Hooks(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    @Before
    public void cleanDatabase() {
        testData.clear();
    }

    @After
    public void closeBrowser(Scenario scenario) {
        if (!browser.isOpened()) {
            return;
        }
        try {
            if (scenario.isFailed()) {
                byte[] screenshot = ((TakesScreenshot) browser.driver()).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", scenario.getName());
            }
        } finally {
            browser.quit();
        }
    }
}
