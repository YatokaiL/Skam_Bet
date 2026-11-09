package ru.skambet.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.ResultsPage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 13: results of finished matches.
 */
public class ResultSteps {

    private final Browser browser;
    private final TestData testData;

    public ResultSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    @Дано("событие {string} завершилось с результатом {string}")
    public void eventFinished(String event, String result) {
        testData.settleEvent(event, SettlementSteps.outcome(result));
    }

    @Тогда("в результатах отображаются матчи в следующем порядке:")
    public void resultsAre(DataTable table) {
        assertThat(resultsPage().results()).containsExactlyElementsOf(table.asMaps());
    }

    @И("событие {string} отсутствует в результатах")
    public void notInResults(String event) {
        assertThat(resultsPage().results()).noneMatch(r -> r.get("событие").equals(event));
    }

    private ResultsPage resultsPage() {
        return new ResultsPage(browser.driver());
    }
}
