package ru.skambet.bdd.support;

import net.thucydides.core.webdriver.DriverSource;
import org.openqa.selenium.WebDriver;

/**
 * Serenity opens the browser through this class (webdriver.driver = provided in serenity.conf),
 * so the options of DriverFactory (-Dheadless, -Dbrowser) work the same way as before.
 */
public class SkamBetDriverSource implements DriverSource {

    @Override
    public WebDriver newDriver() {
        return DriverFactory.create();
    }

    @Override
    public boolean takesScreenshots() {
        return true;
    }
}
