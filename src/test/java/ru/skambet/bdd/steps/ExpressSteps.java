package ru.skambet.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.ExpressPage;
import ru.skambet.bdd.pages.MyBetsPage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 8: express bet. Amount and potential win steps are shared with the single bet (BetSteps).
 */
public class ExpressSteps {

    private final Browser browser;
    private final TestData testData;

    public ExpressSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    @Когда("пользователь добавляет в экспресс исход {string} события {string}")
    public void addToExpress(String outcome, String event) {
        expressPage().addOutcome(event, outcome);
    }

    @Когда("пользователь убирает из экспресса событие {string}")
    public void removeFromExpress(String event) {
        expressPage().removeEvent(event);
    }

    @Тогда("^в купоне экспресса (\\d+) событи(?:е|я|й)$")
    public void legsCount(int count) {
        assertThat(expressPage().legs()).hasSize(count);
    }

    @И("в купоне экспресса есть событие {string} с исходом {string} и коэффициентом {string}")
    public void couponHasLeg(String event, String outcome, String odds) {
        assertThat(expressPage().legs()).contains(Map.of("событие", event, "исход", outcome, "коэффициент", odds));
    }

    @И("общий коэффициент экспресса равен {string}")
    public void totalOdds(String odds) {
        assertThat(expressPage().totalOdds()).isEqualTo(odds);
    }

    @И("в списке экспрессов есть экспресс:")
    public void expressIsInList(DataTable table) {
        Map<String, String> expected = table.asMap(String.class, String.class);
        assertThat(new MyBetsPage(browser.driver()).expresses())
                .anySatisfy(express -> assertThat(express).containsAllEntriesOf(expected));
    }

    @И("в базе данных нет экспрессов пользователя {string}")
    public void noExpressesInDb(String username) {
        assertThat(testData.expressCountInDb(username)).as("expresses of %s in db", username).isZero();
    }

    private ExpressPage expressPage() {
        return new ExpressPage(browser.driver());
    }
}
