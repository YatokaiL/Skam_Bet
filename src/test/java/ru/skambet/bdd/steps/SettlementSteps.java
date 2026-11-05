package ru.skambet.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import org.openqa.selenium.By;
import ru.skambet.bdd.pages.AnyPage;
import ru.skambet.bdd.pages.SettlementPage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;
import ru.skambet.domain.BetStatus;
import ru.skambet.domain.Outcome;
import ru.skambet.domain.Sport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 6: settlement of bets. Also data steps for bets placed earlier (used by features 8 and 10).
 */
public class SettlementSteps {

    private final Browser browser;
    private final TestData testData;

    public SettlementSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    @Дано("зарегистрирован администратор {string} с паролем {string}")
    public void adminExists(String username, String password) {
        testData.createAdmin(username, password);
    }

    @Дано("в системе есть начавшиеся события:")
    public void startedEventsExist(DataTable table) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        for (Map<String, String> row : table.asMaps()) {
            testData.createEvent(
                    Sport.byTitle(row.get("вид спорта"))
                            .orElseThrow(() -> new IllegalArgumentException("Unknown sport: " + row.get("вид спорта"))),
                    row.get("хозяева"),
                    row.get("гости"),
                    now.minusHours(Long.parseLong(row.get("началось, ч назад"))),
                    odds(row.get("П1")),
                    odds(row.get("X")),
                    odds(row.get("П2")));
        }
    }

    @Дано("у пользователя {string} есть ставка {int} ₽ на исход {string} в событии {string}")
    public void userHasBet(String username, int amount, String outcome, String event) {
        testData.createBet(username, event, outcome(outcome), BigDecimal.valueOf(amount));
    }

    @Дано("у пользователя {string} есть экспресс на {int} ₽:")
    public void userHasExpress(String username, int amount, DataTable table) {
        Map<String, Outcome> legs = new LinkedHashMap<>();
        for (Map<String, String> row : table.asMaps()) {
            legs.put(row.get("событие"), outcome(row.get("исход")));
        }
        testData.createExpress(username, BigDecimal.valueOf(amount), legs);
    }

    @Когда("администратор указывает результат {string} для события {string}")
    public void settle(String result, String event) {
        settlementPage().settle(event, result);
    }

    @Тогда("событие {string} отображается в рассчитанных с результатом {string}")
    public void settledWithResult(String event, String result) {
        assertThat(settlementPage().settledResult(event)).isEqualTo(result);
    }

    @И("событие {string} отсутствует в списке на расчёт")
    public void notInSettlementList(String event) {
        assertThat(settlementPage().eventsToSettle()).doesNotContain(event);
    }

    @И("событие {string} есть в списке на расчёт")
    public void inSettlementList(String event) {
        assertThat(settlementPage().eventsToSettle()).contains(event);
    }

    @И("для события {string} можно выбрать результаты:")
    public void possibleResults(String event, List<String> expected) {
        assertThat(settlementPage().possibleResults(event)).containsExactlyElementsOf(expected);
    }

    @И("в базе данных ставка пользователя {string} на событие {string} имеет статус {string}")
    public void betStatusInDb(String username, String event, String status) {
        assertThat(testData.betStatusInDb(username, event)).isEqualTo(status(status).name());
    }

    @И("в базе данных экспресс пользователя {string} имеет статус {string}")
    public void expressStatusInDb(String username, String status) {
        assertThat(testData.expressStatusInDb(username)).isEqualTo(status(status).name());
    }

    @И("в меню нет раздела {string}")
    public void noMenuItem(String text) {
        new AnyPage(browser.driver()).heading(); // waits until the page is loaded
        assertThat(browser.driver().findElements(By.xpath("//nav//a[normalize-space()='" + text + "']")))
                .as("menu item '%s'", text)
                .isEmpty();
    }

    private SettlementPage settlementPage() {
        return new SettlementPage(browser.driver());
    }

    static Outcome outcome(String label) {
        return Arrays.stream(Outcome.values())
                .filter(o -> o.getLabel().equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown outcome: " + label));
    }

    private static BetStatus status(String title) {
        return Arrays.stream(BetStatus.values())
                .filter(s -> s.getTitle().equals(title))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown bet status: " + title));
    }

    private static BigDecimal odds(String value) {
        return value == null || value.equals("-") ? null : new BigDecimal(value);
    }
}
