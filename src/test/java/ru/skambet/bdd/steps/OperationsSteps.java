package ru.skambet.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.OperationsPage;
import ru.skambet.bdd.support.Browser;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 15: history of operations.
 */
public class OperationsSteps {

    private final Browser browser;

    public OperationsSteps(Browser browser) {
        this.browser = browser;
    }

    @Когда("пользователь выбирает тип операций {string}")
    public void selectType(String type) {
        operationsPage().selectType(type);
    }

    @Тогда("в истории операций отображаются операции в следующем порядке:")
    public void operationsAre(DataTable table) {
        assertThat(operationsPage().operations()).containsExactlyElementsOf(table.asMaps());
    }

    private OperationsPage operationsPage() {
        return new OperationsPage(browser.driver());
    }
}
