package ru.skambet.bdd.steps;

import io.cucumber.java.ru.Когда;
import ru.skambet.bdd.pages.AnnulmentForm;
import ru.skambet.bdd.support.Browser;

/**
 * Feature 17: annulment of an event.
 */
public class AnnulmentSteps {

    private final Browser browser;

    public AnnulmentSteps(Browser browser) {
        this.browser = browser;
    }

    @Когда("администратор аннулирует событие {string} с причиной {string}")
    public void annul(String event, String reason) {
        new AnnulmentForm(browser.driver()).annul(event, reason);
    }
}
