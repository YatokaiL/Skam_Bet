package ru.skambet.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import ru.skambet.bdd.pages.RegistrationPage;
import ru.skambet.bdd.support.Browser;

public class RegistrationSteps {

    private final Browser browser;

    public RegistrationSteps(Browser browser) {
        this.browser = browser;
    }

    @Когда("посетитель заполняет форму регистрации:")
    public void fillRegistrationForm(DataTable form) {
        registrationPage().fill(form.asMap(String.class, String.class));
    }

    @И("^(?:посетитель )?отмечает согласие с правилами$")
    public void acceptRules() {
        registrationPage().acceptRules();
    }

    private RegistrationPage registrationPage() {
        return new RegistrationPage(browser.driver());
    }
}
