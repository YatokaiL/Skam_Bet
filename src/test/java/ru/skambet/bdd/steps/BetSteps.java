package ru.skambet.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.BetSlipPage;
import ru.skambet.bdd.pages.HomePage;
import ru.skambet.bdd.pages.MyBetsPage;
import ru.skambet.bdd.support.Browser;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BetSteps {

    private final Browser browser;

    public BetSteps(Browser browser) {
        this.browser = browser;
    }

    @Когда("^(?:пользователь|посетитель) выбирает исход \"([^\"]*)\" в событии \"([^\"]*)\"$")
    public void chooseOutcome(String outcome, String event) {
        new HomePage(browser.driver()).chooseOutcome(event, outcome);
    }

    @И("в купоне указано событие {string}, исход {string} и коэффициент {string}")
    public void couponContains(String event, String outcome, String odds) {
        BetSlipPage slip = betSlip();
        assertThat(slip.event()).isEqualTo(event);
        assertThat(slip.outcome()).isEqualTo(outcome);
        assertThat(slip.odds()).isEqualTo(odds);
    }

    @Когда("^(?:пользователь )?вводит сумму ставки \"([^\"]*)\"$")
    public void enterAmount(String amount) {
        betSlip().enterAmount(amount);
    }

    @Тогда("возможный выигрыш равен {string}")
    public void potentialWinIs(String expected) {
        betSlip().shouldShowPotentialWin(expected);
    }

    @И("в списке ставок есть ставка:")
    public void betIsInList(DataTable table) {
        Map<String, String> expected = table.asMap(String.class, String.class);
        assertThat(new MyBetsPage(browser.driver()).bets())
                .anySatisfy(bet -> assertThat(bet).containsAllEntriesOf(expected));
    }

    private BetSlipPage betSlip() {
        return new BetSlipPage(browser.driver());
    }
}
