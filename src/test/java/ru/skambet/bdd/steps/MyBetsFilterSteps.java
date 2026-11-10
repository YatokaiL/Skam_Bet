package ru.skambet.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.MyBetsPage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 14: filter of "Мои ставки" by status.
 */
public class MyBetsFilterSteps {

    private final Browser browser;
    private final TestData testData;

    public MyBetsFilterSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    @Дано("ставка пользователя {string} на событие {string} рассчитана со статусом {string}")
    public void betHasStatus(String username, String event, String status) {
        testData.setBetStatus(username, event, SettlementSteps.status(status));
    }

    @Когда("пользователь выбирает статус ставок {string}")
    public void selectStatus(String status) {
        myBets().selectStatus(status);
    }

    @Тогда("во вкладках статусов ставок отображается:")
    public void statusCounts(DataTable table) {
        Map<String, Integer> expected = new LinkedHashMap<>();
        table.asMap(String.class, String.class).forEach((tab, count) -> expected.put(tab, Integer.parseInt(count)));
        assertThat(myBets().statusCounts()).containsExactlyEntriesOf(expected);
    }

    @И("выбран статус ставок {string}")
    public void activeStatus(String status) {
        assertThat(myBets().activeStatus()).isEqualTo(status);
    }

    @И("^в списке экспрессов (\\d+) экспресс(?:а|ов)?$")
    public void expressesCount(int count) {
        assertThat(myBets().expresses()).hasSize(count);
    }

    @И("в списке ставок нет ставки на событие {string}")
    public void noBetOn(String event) {
        assertThat(myBets().bets()).noneMatch(b -> b.get("событие").equals(event));
    }

    @И("поставлено {string}, выиграно {string}")
    public void summary(String placed, String won) {
        MyBetsPage page = myBets();
        assertThat(page.placed()).as("placed").isEqualTo(placed);
        assertThat(page.won()).as("won").isEqualTo(won);
    }

    private MyBetsPage myBets() {
        return new MyBetsPage(browser.driver());
    }
}
