package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyBetsPage extends BasePage {

    private static final By ROWS = By.cssSelector("#bets-table .bet-row");
    // text of a status tab: "В игре (2)"
    private static final Pattern TAB = Pattern.compile("(.+) \\((\\d+)\\)");

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
        // the table is not shown when the chosen status has no single bets
        visible(By.tagName("h1"));
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

    // feature 18: cashout of a single bet

    public void cashOut(String eventTitle) {
        clickAndWaitForReload(By.xpath(rowXpath(eventTitle) + "//button[normalize-space()='Выкупить']"));
    }

    /**
     * Waits until the amount in the row becomes the expected one - it is updated by the page itself.
     */
    public void shouldOfferCashout(String eventTitle, String amount) {
        waitForExactText(By.xpath(rowXpath(eventTitle) + "//span[contains(@class,'cashout-amount')]"), amount,
                "Cashout amount for " + eventTitle);
    }

    /**
     * Waits until the cashout button disappears from the row (also without reloading the page).
     */
    public void shouldNotOfferCashout(String eventTitle) {
        By button = By.xpath(rowXpath(eventTitle) + "//button[normalize-space()='Выкупить']");
        visible(By.id("bets-table"));
        try {
            wait.until(d -> d.findElements(button).isEmpty());
        } catch (org.openqa.selenium.TimeoutException e) {
            throw new AssertionError("Cashout is still offered for " + eventTitle);
        }
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
        visible(By.tagName("h1"));
        return driver.findElements(By.cssSelector("#expresses-table .express-row")).stream()
                .map(row -> {
                    Map<String, String> express = new LinkedHashMap<>();
                    EXPRESS_COLUMNS.forEach((name, cssClass) ->
                            express.put(name, row.findElement(By.className(cssClass)).getText().trim()));
                    return express;
                })
                .toList();
    }

    // feature 14: filter by status

    public void selectStatus(String title) {
        By tab = By.xpath("//div[@id='status-tabs']/a[starts-with(normalize-space(), '" + title + " (')]");
        clickAndWaitForReload(tab);
        wait.until(ExpectedConditions.attributeContains(tab, "class", "active"));
    }

    /**
     * @return tab title -> number in brackets, e.g. "В игре" -> 2
     */
    public Map<String, Integer> statusCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String text : texts(By.cssSelector("#status-tabs .tab"))) {
            Matcher m = TAB.matcher(text);
            if (!m.matches()) {
                throw new IllegalStateException("Unexpected tab text: " + text);
            }
            counts.put(m.group(1), Integer.parseInt(m.group(2)));
        }
        return counts;
    }

    public String activeStatus() {
        String text = visible(By.cssSelector("#status-tabs .tab.active")).getText().trim();
        return text.substring(0, text.lastIndexOf(" ("));
    }

    public String placed() {
        return visible(By.id("summary-placed")).getText().trim();
    }

    public String won() {
        return visible(By.id("summary-won")).getText().trim();
    }
}
