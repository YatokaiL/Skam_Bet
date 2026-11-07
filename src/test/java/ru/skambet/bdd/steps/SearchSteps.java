package ru.skambet.bdd.steps;

import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.HomePage;
import ru.skambet.bdd.support.Browser;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 11: search in the line. Checks of the line itself are in EventSteps.
 */
public class SearchSteps {

    private final Browser browser;

    public SearchSteps(Browser browser) {
        this.browser = browser;
    }

    @Когда("посетитель ищет {string}")
    public void search(String query) {
        homePage().search(query);
    }

    @Когда("посетитель сбрасывает поиск")
    public void resetSearch() {
        homePage().resetSearch();
    }

    @Тогда("найдено событий: {int}")
    public void foundEvents(int count) {
        assertThat(homePage().eventTitles()).hasSize(count);
    }

    @И("строка поиска пуста")
    public void searchIsEmpty() {
        assertThat(homePage().searchQuery()).isEmpty();
    }

    private HomePage homePage() {
        return new HomePage(browser.driver());
    }
}
