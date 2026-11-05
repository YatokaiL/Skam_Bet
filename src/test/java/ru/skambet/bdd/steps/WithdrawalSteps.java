package ru.skambet.bdd.steps;

import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import ru.skambet.bdd.pages.WithdrawalPage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 7: withdrawal of money.
 */
public class WithdrawalSteps {

    private final Browser browser;
    private final TestData testData;

    public WithdrawalSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    @Когда("пользователь вводит сумму вывода {string} и номер карты {string}")
    public void enterWithdrawal(String amount, String cardNumber) {
        withdrawalPage().fill(amount, cardNumber);
    }

    @И("в истории выводов есть запись на сумму {string} на карту {string}")
    public void historyHasRecord(String amount, String cardMask) {
        assertThat(withdrawalPage().hasHistoryRecord(amount, cardMask))
                .as("withdrawal %s to %s in history", amount, cardMask)
                .isTrue();
    }

    @И("^в истории выводов (\\d+) запис(?:ь|и|ей)$")
    public void historySize(int count) {
        assertThat(withdrawalPage().historySize()).isEqualTo(count);
    }

    @И("история выводов пуста")
    public void historyIsEmpty() {
        assertThat(withdrawalPage().isHistoryEmpty()).as("withdrawal history is empty").isTrue();
    }

    @И("в базе данных нет выводов пользователя {string}")
    public void noWithdrawalsInDb(String username) {
        assertThat(testData.withdrawalsCountInDb(username)).as("withdrawals of %s in db", username).isZero();
    }

    private WithdrawalPage withdrawalPage() {
        return new WithdrawalPage(browser.driver());
    }
}
