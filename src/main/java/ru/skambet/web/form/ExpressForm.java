package ru.skambet.web.form;

import java.math.BigDecimal;

public class ExpressForm {

    private String amount;
    // feature 16: odds shown in the coupon
    private BigDecimal expectedOdds;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public BigDecimal getExpectedOdds() {
        return expectedOdds;
    }

    public void setExpectedOdds(BigDecimal expectedOdds) {
        this.expectedOdds = expectedOdds;
    }
}
