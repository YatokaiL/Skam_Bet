package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Map;

public class RegistrationPage extends BasePage {

    // names of the fields as they are written in the feature files
    private static final Map<String, By> FIELDS = Map.of(
            "логин", By.id("username"),
            "email", By.id("email"),
            "дата рождения", By.id("birthDate"),
            "пароль", By.id("password"),
            "повтор пароля", By.id("confirmPassword"));

    private static final By ACCEPT_RULES = By.id("acceptRules");

    public RegistrationPage(WebDriver driver) {
        super(driver);
    }

    public void fill(Map<String, String> values) {
        values.forEach((field, value) -> {
            By locator = FIELDS.get(field);
            if (locator == null) {
                throw new IllegalArgumentException("Unknown registration field: " + field);
            }
            type(locator, value);
        });
    }

    public void acceptRules() {
        WebElement checkbox = visible(ACCEPT_RULES);
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
    }
}
