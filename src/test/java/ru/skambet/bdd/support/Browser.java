package ru.skambet.bdd.support;

import io.cucumber.spring.ScenarioScope;
import net.thucydides.core.webdriver.ThucydidesWebDriverSupport;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Browser of the current scenario, shared between all step classes.
 * The driver is managed by Serenity: the window is opened on first use, a screenshot is taken
 * after every step for the report, and the window is closed after the scenario.
 */
@Component
@ScenarioScope
public class Browser {

    private final int port;

    public Browser(@Value("${local.server.port}") int port) {
        this.port = port;
    }

    public WebDriver driver() {
        return ThucydidesWebDriverSupport.getDriver();
    }

    public void open(String path) {
        driver().get(baseUrl() + path);
    }

    public String baseUrl() {
        return "http://localhost:" + port;
    }

    public boolean isOpened() {
        return ThucydidesWebDriverSupport.isDriverInstantiated();
    }
}
