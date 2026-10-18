package ru.skambet.bdd.support;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
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

    /**
     * -Dstep.delay=1500 makes a pause after every step, to watch what the test is doing.
     */
    @AfterStep
    public void slowDown() {
        long delay = Long.getLong("step.delay", 0L);
        if (delay > 0 && browser.isOpened()) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
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
