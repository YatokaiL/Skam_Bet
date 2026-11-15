package ru.skambet.service;

import java.math.BigDecimal;

/**
 * Feature 18: the odds changed after the page with the cashout amount was shown.
 */
public class CashoutChangedException extends BetRejectedException {

    private final BigDecimal oldAmount;
    private final BigDecimal newAmount;

    public CashoutChangedException(BigDecimal oldAmount, BigDecimal newAmount) {
        super("Cashout amount changed: " + oldAmount + " -> " + newAmount);
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
    }

    public BigDecimal getOldAmount() {
        return oldAmount;
    }

    public BigDecimal getNewAmount() {
        return newAmount;
    }
}
