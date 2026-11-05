package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

/**
 * Feature 6: administrator page with events waiting for the result.
 */
public class SettlementPage extends BasePage {

    private static final By TO_SETTLE_TITLES = By.cssSelector("#settle-table .settle-row .event-title");

    public SettlementPage(WebDriver driver) {
        super(driver);
    }

    public void settle(String eventTitle, String resultLabel) {
        WebElement row = row("settle-row", eventTitle);
        new Select(row.findElement(By.name("result"))).selectByVisibleText(resultLabel);
        row.findElement(By.xpath(".//button[normalize-space()='Рассчитать']")).click();
    }

    public List<String> possibleResults(String eventTitle) {
        return new Select(row("settle-row", eventTitle).findElement(By.name("result"))).getOptions().stream()
                .map(o -> o.getText().trim())
                .toList();
    }

    public List<String> eventsToSettle() {
        visible(By.tagName("h1"));
        return texts(TO_SETTLE_TITLES);
    }

    public String settledResult(String eventTitle) {
        return row("settled-row", eventTitle).findElement(By.className("event-result")).getText().trim();
    }

    private WebElement row(String rowClass, String eventTitle) {
        return visible(By.xpath("//tr[contains(@class,'" + rowClass + "')][td[contains(@class,'event-title')]"
                + "[normalize-space()='" + eventTitle + "']]"));
    }
}
