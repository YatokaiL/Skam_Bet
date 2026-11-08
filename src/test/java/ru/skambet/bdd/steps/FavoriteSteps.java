package ru.skambet.bdd.steps;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.HomePage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 12: favorite events.
 */
public class FavoriteSteps {

    private final Browser browser;
    private final TestData testData;

    public FavoriteSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    @Дано("у пользователя {string} в избранном событие {string}")
    public void userHasFavorite(String username, String event) {
        testData.addFavorite(username, event);
    }

    @Когда("^пользователь (?:добавляет в избранное|убирает из избранного) событие \"([^\"]*)\"$")
    public void toggleFavorite(String event) {
        homePage().toggleFavorite(event);
    }

    @Тогда("событие {string} отмечено как избранное")
    public void isFavorite(String event) {
        assertThat(homePage().isFavorite(event)).as("%s is favorite", event).isTrue();
    }

    @Тогда("событие {string} не отмечено как избранное")
    public void isNotFavorite(String event) {
        assertThat(homePage().isFavorite(event)).as("%s is favorite", event).isFalse();
    }

    @И("на странице нет кнопок избранного")
    public void noFavorites() {
        assertThat(homePage().hasFavorites()).as("favorite buttons or tab").isFalse();
    }

    private HomePage homePage() {
        return new HomePage(browser.driver());
    }
}
