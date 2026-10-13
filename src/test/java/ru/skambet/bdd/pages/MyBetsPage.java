package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MyBetsPage extends BasePage {

    private static final By ROWS = By.cssSelector("#bets-table .bet-row");

    // column name from the feature file -> css class of the cell
    private static final Map<String, String> COLUMNS = Map.of(
            "событие", "bet-event",
            "исход", "bet-outcome",
            "коэффициент", "bet-odds",
            "сумма", "bet-amount",
            "возможный выигрыш", "bet-win",
            "статус", "bet-status");

    public MyBetsPage(WebDriver driver) {
        super(driver);
    }

    public List<Map<String, String>> bets() {
        visible(By.id("bets-table"));
        return driver.findElements(ROWS).stream().map(MyBetsPage::toMap).toList();
    }

    private static Map<String, String> toMap(WebElement row) {
        Map<String, String> bet = new LinkedHashMap<>();
        COLUMNS.forEach((name, cssClass) ->
                bet.put(name, row.findElement(By.className(cssClass)).getText().trim()));
        return bet;
    }
}
