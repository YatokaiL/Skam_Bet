package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class BetSlipPage extends BasePage {

    private static final By EVENT = By.id("coupon-event");
    private static final By OUTCOME = By.id("coupon-outcome");
    private static final By ODDS = By.id("coupon-odds");
    private static final By AMOUNT = By.id("amount");
    private static final By POTENTIAL_WIN = By.id("potential-win");

    public BetSlipPage(WebDriver driver) {
        super(driver);
    }

    public String event() {
        return visible(EVENT).getText().trim();
    }

    public String outcome() {
        return visible(OUTCOME).getText().trim();
    }

    public String odds() {
        return visible(ODDS).getText().trim();
    }

    public void enterAmount(String amount) {
        type(AMOUNT, amount);
    }

    public void shouldShowPotentialWin(String expected) {
        waitForExactText(POTENTIAL_WIN, expected, "Potential win");
    }
}
