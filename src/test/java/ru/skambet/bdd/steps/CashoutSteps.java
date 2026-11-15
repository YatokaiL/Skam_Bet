package ru.skambet.bdd.steps;

import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.MyBetsPage;
import ru.skambet.bdd.support.Browser;

/**
 * Feature 18: cashout of a bet.
 */
public class CashoutSteps {

    private final Browser browser;

    public CashoutSteps(Browser browser) {
        this.browser = browser;
    }

    @Тогда("сумма выкупа ставки на событие {string} равна {string}")
    public void cashoutOffer(String event, String amount) {
        myBets().shouldOfferCashout(event, amount);
    }

    @Тогда("для ставки на событие {string} выкуп недоступен")
    public void noCashout(String event) {
        myBets().shouldNotOfferCashout(event);
    }

    @Когда("пользователь выкупает ставку на событие {string}")
    public void cashOut(String event) {
        myBets().cashOut(event);
    }

    private MyBetsPage myBets() {
        return new MyBetsPage(browser.driver());
    }
}
