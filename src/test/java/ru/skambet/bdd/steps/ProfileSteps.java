package ru.skambet.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.ProfilePage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 9: personal account.
 */
public class ProfileSteps {

    private final Browser browser;
    private final TestData testData;

    public ProfileSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    @Когда("пользователь нажимает на свой логин в шапке сайта")
    public void openProfileFromHeader() {
        profilePage().openFromHeader();
    }

    @Тогда("в личном кабинете отображаются данные:")
    public void profileShowsData(DataTable table) {
        ProfilePage page = profilePage();
        table.asMap(String.class, String.class).forEach(page::shouldShowData);
    }

    @Когда("пользователь вводит новый email {string}")
    public void enterNewEmail(String email) {
        profilePage().enterNewEmail(email);
    }

    @Когда("пользователь заполняет форму смены пароля:")
    public void fillPasswordForm(DataTable table) {
        Map<String, String> form = table.asMap(String.class, String.class);
        profilePage().fillPasswordForm(form.get("текущий пароль"), form.get("новый пароль"),
                form.get("повтор пароля"));
    }

    @И("в базе данных email пользователя {string} равен {string}")
    public void emailInDb(String username, String email) {
        assertThat(testData.emailInDb(username)).isEqualTo(email);
    }

    private ProfilePage profilePage() {
        return new ProfilePage(browser.driver());
    }
}
