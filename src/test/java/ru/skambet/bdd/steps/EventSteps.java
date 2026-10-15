package ru.skambet.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.HomePage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;
import ru.skambet.domain.Sport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EventSteps {

    private static final String TITLE_SEPARATOR = " — ";

    private final Browser browser;
    private final TestData testData;

    public EventSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    @Дано("в линии есть события:")
    public void lineHasEvents(DataTable table) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        for (Map<String, String> row : table.asMaps()) {
            testData.createEvent(
                    sport(row.get("вид спорта")),
                    row.get("хозяева"),
                    row.get("гости"),
                    now.plusHours(Long.parseLong(row.get("начало через, ч"))),
                    odds(row.get("П1")),
                    odds(row.get("X")),
                    odds(row.get("П2")));
        }
    }

    @Дано("в системе есть событие {string} по виду спорта {string}, которое началось {int} ч назад")
    public void startedEventExists(String title, String sport, int hoursAgo) {
        String[] teams = title.split(TITLE_SEPARATOR);
        testData.createEvent(sport(sport), teams[0], teams[1], LocalDateTime.now().minusHours(hoursAgo),
                new BigDecimal("2.00"), new BigDecimal("3.00"), new BigDecimal("3.50"));
    }

    @Когда("посетитель выбирает вид спорта {string}")
    public void selectSport(String sport) {
        homePage().selectSport(sport);
    }

    @Тогда("в линии отображаются события в следующем порядке:")
    public void eventsAreShownInOrder(List<String> expected) {
        assertThat(homePage().eventTitles()).containsExactlyElementsOf(expected);
    }

    @Тогда("^в линии отображается (\\d+) событи(?:е|я|й)$")
    public void eventsCount(int count) {
        assertThat(homePage().eventTitles()).hasSize(count);
    }

    @И("для события {string} отображаются коэффициенты:")
    public void eventHasOdds(String event, DataTable table) {
        HomePage page = homePage();
        table.asMaps().get(0).forEach((outcome, odds) ->
                assertThat(page.odds(event, outcome)).as("odds for %s", outcome).isEqualTo(odds));
    }

    @И("для события {string} ставка на ничью недоступна")
    public void drawIsNotAvailable(String event) {
        HomePage page = homePage();
        assertThat(page.isOutcomeClickable(event, "X")).isFalse();
        assertThat(page.odds(event, "X")).isEqualTo("—");
    }

    @И("событие {string} отсутствует в линии")
    public void eventIsAbsent(String event) {
        assertThat(homePage().eventTitles()).doesNotContain(event);
    }

    @И("вкладка {string} выделена как активная")
    public void tabIsActive(String tab) {
        assertThat(homePage().activeTab()).isEqualTo(tab);
    }

    private HomePage homePage() {
        return new HomePage(browser.driver());
    }

    private static Sport sport(String title) {
        return Sport.byTitle(title).orElseThrow(() -> new IllegalArgumentException("Unknown sport: " + title));
    }

    private static BigDecimal odds(String value) {
        return value == null || value.equals("-") ? null : new BigDecimal(value);
    }
}
