package ru.skambet.bdd.steps;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.AnyPage;
import ru.skambet.bdd.support.Browser;

import java.util.Map;

/**
 * Navigation, buttons and messages - used by all features.
 */
public class CommonSteps {

    private static final Map<String, String> PAGES = Map.ofEntries(
            Map.entry("Линия", "/"),
            Map.entry("Вход", "/login"),
            Map.entry("Регистрация", "/register"),
            Map.entry("Пополнение баланса", "/deposit"),
            Map.entry("Мои ставки", "/bets/my"),
            Map.entry("Экспресс", "/express"),
            Map.entry("Вывод средств", "/withdraw"),
            Map.entry("Личный кабинет", "/profile"),
            Map.entry("Расчёт событий", "/admin/events"),
            Map.entry("Результаты", "/results"),
            Map.entry("История операций", "/operations"),
            Map.entry("Управление линией", "/admin/line"),
            Map.entry("Верификация", "/verification"),
            Map.entry("Верификация пользователей", "/admin/verification"));

    private final Browser browser;

    public CommonSteps(Browser browser) {
        this.browser = browser;
    }

    @Дано("посетитель открыл страницу {string}")
    public void visitorOpenedPage(String page) {
        openPage(page);
    }

    @Когда("^(?:посетитель|пользователь) открывает страницу \"([^\"]*)\"(?: по прямой ссылке)?$")
    public void visitorOpensPage(String page) {
        openPage(page);
    }

    @Когда("пользователь переходит в раздел {string}")
    public void userGoesToSection(String linkText) {
        page().clickNavLink(linkText);
    }

    @Когда("^(?:посетитель |пользователь )?нажимает кнопку \"([^\"]*)\"$")
    public void pressButton(String text) {
        page().clickButton(text);
    }

    @Тогда("открывается страница {string}")
    public void pageIsOpened(String heading) {
        page().waitForHeading(heading);
    }

    @И("посетитель остаётся на странице {string}")
    public void visitorStaysOnPage(String heading) {
        page().waitForHeading(heading);
    }

    @Тогда("отображается сообщение {string}")
    public void messageIsShown(String message) {
        page().shouldShowMessage(message);
    }

    @Тогда("отображается ошибка {string}")
    public void errorIsShown(String error) {
        page().shouldShowError(error);
    }

    private void openPage(String page) {
        String path = PAGES.get(page);
        if (path == null) {
            throw new IllegalArgumentException("Unknown page: " + page);
        }
        browser.open(path);
    }

    private AnyPage page() {
        return new AnyPage(browser.driver());
    }
}
