package ru.skambet.bdd.pages;

import org.openqa.selenium.WebDriver;

/**
 * Current page when its type doesn't matter: header, alerts and buttons are on every page.
 */
public class AnyPage extends BasePage {

    public AnyPage(WebDriver driver) {
        super(driver);
    }
}
