package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class DepositPage extends BasePage {

    private static final By AMOUNT = By.id("amount");
    private static final By CARD_NUMBER = By.id("cardNumber");
    private static final By CARD_EXPIRY = By.id("cardExpiry");
    private static final By CARD_CVV = By.id("cardCvv");
    private static final By HISTORY_ROWS = By.cssSelector("#deposits-table .deposit-row");
    private static final By NO_DEPOSITS = By.id("no-deposits");

    public DepositPage(WebDriver driver) {
        super(driver);
    }

    public void fill(String amount, String cardNumber, String expiry, String cvv) {
        type(AMOUNT, amount);
        type(CARD_NUMBER, cardNumber);
        type(CARD_EXPIRY, expiry);
        type(CARD_CVV, cvv);
    }

    public boolean hasHistoryRecord(String amount, String cardMask) {
        visible(By.tagName("h2"));
        List<WebElement> rows = driver.findElements(HISTORY_ROWS);
        return rows.stream().anyMatch(row ->
                row.findElement(By.className("deposit-amount")).getText().trim().equals(amount)
                        && row.findElement(By.className("deposit-card")).getText().trim().equals(cardMask));
    }

    public boolean isHistoryEmpty() {
        visible(By.tagName("h2"));
        return driver.findElements(HISTORY_ROWS).isEmpty() && !driver.findElements(NO_DEPOSITS).isEmpty();
    }
}
