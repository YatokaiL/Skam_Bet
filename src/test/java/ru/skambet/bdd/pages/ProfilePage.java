package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Map;

/**
 * Feature 9: personal account.
 */
public class ProfilePage extends BasePage {

    // names of the data rows as they are written in the feature files
    private static final Map<String, By> DATA = Map.of(
            "логин", By.id("profile-username"),
            "email", By.id("profile-email"),
            "дата рождения", By.id("profile-birth-date"),
            "баланс", By.id("profile-balance"),
            "сделано ставок", By.id("profile-bets"));

    private static final By NEW_EMAIL = By.id("email");
    private static final By CURRENT_PASSWORD = By.id("currentPassword");
    private static final By NEW_PASSWORD = By.id("newPassword");
    private static final By CONFIRM_PASSWORD = By.id("confirmPassword");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    public void shouldShowData(String field, String expected) {
        By locator = DATA.get(field);
        if (locator == null) {
            throw new IllegalArgumentException("Unknown profile field: " + field);
        }
        waitForExactText(locator, expected, "Profile field '" + field + "'");
    }

    public void enterNewEmail(String email) {
        type(NEW_EMAIL, email);
    }

    public void fillPasswordForm(String current, String password, String confirm) {
        type(CURRENT_PASSWORD, current);
        type(NEW_PASSWORD, password);
        type(CONFIRM_PASSWORD, confirm);
    }

    public void openFromHeader() {
        click(By.id("current-user"));
    }
}
