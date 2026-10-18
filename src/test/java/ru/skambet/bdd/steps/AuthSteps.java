package ru.skambet.bdd.steps;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.AnyPage;
import ru.skambet.bdd.pages.LoginPage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {

    private final Browser browser;
    private final TestData testData;

    public AuthSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    @Дано("зарегистрирован пользователь {string} с паролем {string}")
    public void userExists(String username, String password) {
        testData.createUser(username, password, BigDecimal.ZERO);
    }

    @Дано("зарегистрирован пользователь {string} с паролем {string} и балансом {int} ₽")
    public void userWithBalanceExists(String username, String password, int balance) {
        testData.createUser(username, password, BigDecimal.valueOf(balance));
    }

    @Дано("пользователь вошёл в систему с логином {string} и паролем {string}")
    public void userIsLoggedIn(String username, String password) {
        browser.open("/login");
        LoginPage loginPage = new LoginPage(browser.driver());
        loginPage.login(username, password);
        loginPage.shouldShowCurrentUser(username);
    }

    @Когда("^(?:пользователь|посетитель) входит с логином \"([^\"]*)\" и паролем \"([^\"]*)\"$")
    public void userLogsIn(String username, String password) {
        LoginPage loginPage = new LoginPage(browser.driver());
        loginPage.waitForHeading("Вход");
        loginPage.login(username, password);
    }

    @Тогда("в шапке сайта отображается пользователь {string}")
    public void headerShowsUser(String username) {
        new AnyPage(browser.driver()).shouldShowCurrentUser(username);
    }

    @И("баланс пользователя равен {string}")
    public void balanceIs(String balance) {
        new AnyPage(browser.driver()).shouldShowBalance(balance);
    }

    @И("в шапке сайта отображается ссылка {string}")
    public void headerShowsLink(String text) {
        AnyPage page = new AnyPage(browser.driver());
        page.shouldShowHeaderLink(text);
        assertThat(page.isLoggedIn()).as("user must be logged out").isFalse();
    }
}
