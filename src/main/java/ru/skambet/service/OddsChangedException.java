package ru.skambet.service;

import java.math.BigDecimal;

/**
 * Feature 16: the odds were changed by the administrator after the user opened the coupon.
 * The bet is not placed, the user sees the new odds and confirms the bet again.
 */
public class OddsChangedException extends BetRejectedException {

    private final BigDecimal oldOdds;
    private final BigDecimal newOdds;

    public OddsChangedException(BigDecimal oldOdds, BigDecimal newOdds) {
        super("Коэффициент изменился: " + oldOdds.toPlainString() + " → " + newOdds.toPlainString());
        this.oldOdds = oldOdds;
        this.newOdds = newOdds;
    }

    public BigDecimal getOldOdds() {
        return oldOdds;
    }

    public BigDecimal getNewOdds() {
        return newOdds;
    }
}
