package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Feature 7.
 */
public class WithdrawalPage extends BasePage {

    private static final By AMOUNT = By.id("amount");
    private static final By CARD_NUMBER = By.id("cardNumber");
    private static final By HISTORY_ROWS = By.cssSelector("#withdrawals-table .withdrawal-row");
    private static final By NO_WITHDRAWALS = By.id("no-withdrawals");

    public WithdrawalPage(WebDriver driver) {
        super(driver);
    }

    public void fill(String amount, String cardNumber) {
        type(AMOUNT, amount);
        type(CARD_NUMBER, cardNumber);
    }

    public boolean hasHistoryRecord(String amount, String cardMask) {
        visible(By.tagName("h2"));
        List<WebElement> rows = driver.findElements(HISTORY_ROWS);
        return rows.stream().anyMatch(row ->
                row.findElement(By.className("withdrawal-amount")).getText().trim().equals(amount)
                        && row.findElement(By.className("withdrawal-card")).getText().trim().equals(cardMask));
    }

    public int historySize() {
        visible(By.tagName("h2"));
        return driver.findElements(HISTORY_ROWS).size();
    }

    public boolean isHistoryEmpty() {
        visible(By.tagName("h2"));
        return driver.findElements(HISTORY_ROWS).isEmpty() && !driver.findElements(NO_WITHDRAWALS).isEmpty();
    }
}
