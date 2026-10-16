package ru.skambet.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import ru.skambet.bdd.pages.DepositPage;
import ru.skambet.bdd.support.Browser;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositSteps {

    private final Browser browser;

    public DepositSteps(Browser browser) {
        this.browser = browser;
    }

    @Когда("пользователь вводит сумму пополнения {string} и данные карты:")
    public void enterDeposit(String amount, DataTable cardTable) {
        Map<String, String> card = cardTable.asMap(String.class, String.class);
        depositPage().fill(amount, card.get("номер карты"), card.get("срок действия"), card.get("CVV"));
    }

    @И("в истории пополнений есть запись на сумму {string} с карты {string}")
    public void historyHasRecord(String amount, String cardMask) {
        assertThat(depositPage().hasHistoryRecord(amount, cardMask))
                .as("deposit %s from %s in history", amount, cardMask)
                .isTrue();
    }

    @И("история пополнений пуста")
    public void historyIsEmpty() {
        assertThat(depositPage().isHistoryEmpty()).as("deposit history is empty").isTrue();
    }

    private DepositPage depositPage() {
        return new DepositPage(browser.driver());
    }
}
