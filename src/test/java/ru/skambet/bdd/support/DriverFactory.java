package ru.skambet.bdd.support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.util.Map;

/**
 * Browser is chosen with system properties:
 * -Dbrowser=chrome|edge (chrome by default), -Dheadless=true to hide the window.
 * Driver binaries are downloaded by Selenium Manager automatically.
 */
public final class DriverFactory {

    private static final String WINDOW_SIZE = "--window-size=1366,900";

    // disable "save password" bubbles, they only get in the way
    private static final Map<String, Object> PREFS = Map.of(
            "credentials_enable_service", false,
            "profile.password_manager_enabled", false,
            "profile.password_manager_leak_detection", false);

    private DriverFactory() {
    }

    public static WebDriver create() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        String browser = System.getProperty("browser", "chrome").toLowerCase();

        if (browser.equals("edge")) {
            EdgeOptions options = new EdgeOptions();
            options.addArguments(WINDOW_SIZE);
            options.setExperimentalOption("prefs", PREFS);
            if (headless) {
                options.addArguments("--headless=new");
            }
            return new EdgeDriver(options);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments(WINDOW_SIZE);
        options.setExperimentalOption("prefs", PREFS);
        if (headless) {
            options.addArguments("--headless=new");
        }
        return new ChromeDriver(options);
    }
}
