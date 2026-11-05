package ru.skambet.bdd.steps;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import ru.skambet.bdd.pages.MyBetsPage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 10: cancellation of a bet.
 */
public class CancelBetSteps {

    private final Browser browser;
    private final TestData testData;

    public CancelBetSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    @Когда("пользователь отменяет ставку на событие {string}")
    public void cancelBet(String event) {
        myBetsPage().cancelBet(event);
    }

    @И("для ставки на событие {string} доступна отмена")
    public void canCancel(String event) {
        assertThat(myBetsPage().canCancel(event)).as("cancel button for %s", event).isTrue();
    }

    @И("для ставки на событие {string} нет кнопки отмены")
    public void cannotCancel(String event) {
        assertThat(myBetsPage().canCancel(event)).as("cancel button for %s", event).isFalse();
    }

    @Дано("событие {string} уже началось")
    public void eventHasStarted(String event) {
        testData.moveEventToPast(event);
    }

    private MyBetsPage myBetsPage() {
        return new MyBetsPage(browser.driver());
    }
}
