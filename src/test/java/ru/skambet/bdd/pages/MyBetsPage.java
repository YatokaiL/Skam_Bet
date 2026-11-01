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

    // feature 10: cancel button in the row of a single bet

    public void cancelBet(String eventTitle) {
        click(By.xpath(rowXpath(eventTitle) + "//button[normalize-space()='Отменить']"));
    }

    public boolean canCancel(String eventTitle) {
        visible(By.id("bets-table"));
        return !driver.findElements(By.xpath(rowXpath(eventTitle) + "//button[normalize-space()='Отменить']"))
                .isEmpty();
    }

    private static String rowXpath(String eventTitle) {
        return "//table[@id='bets-table']//tr[contains(@class,'bet-row')][td[contains(@class,'bet-event')]"
                + "[normalize-space()='" + eventTitle + "']]";
    }

    // feature 8: table of express bets

    private static final Map<String, String> EXPRESS_COLUMNS = Map.of(
            "события", "express-events",
            "коэффициент", "express-odds",
            "сумма", "express-amount",
            "возможный выигрыш", "express-win",
            "статус", "express-status");

    public List<Map<String, String>> expresses() {
        visible(By.id("expresses-table"));
        return driver.findElements(By.cssSelector("#expresses-table .express-row")).stream()
                .map(row -> {
                    Map<String, String> express = new LinkedHashMap<>();
                    EXPRESS_COLUMNS.forEach((name, cssClass) ->
                            express.put(name, row.findElement(By.className(cssClass)).getText().trim()));
                    return express;
                })
                .toList();
    }
}
