package ru.skambet.bdd.support;

import io.cucumber.spring.ScenarioScope;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * One browser per scenario, shared between all step classes.
 * The window is opened lazily on first use.
 */
@Component
@ScenarioScope
public class Browser {

    private final int port;
    private WebDriver driver;

    public Browser(@Value("${local.server.port}") int port) {
        this.port = port;
    }

    public WebDriver driver() {
        if (driver == null) {
            driver = DriverFactory.create();
        }
        return driver;
    }

    public void open(String path) {
        driver().get(baseUrl() + path);
    }

    public String baseUrl() {
        return "http://localhost:" + port;
    }

    public boolean isOpened() {
        return driver != null;
    }

    public void quit() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
