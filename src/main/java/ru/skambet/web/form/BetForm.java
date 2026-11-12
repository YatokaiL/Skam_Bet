package ru.skambet.web.form;

import ru.skambet.domain.Outcome;

import java.math.BigDecimal;

public class BetForm {

    private Long eventId;
    private Outcome outcome;
    private String amount;
    // feature 16: odds shown in the coupon
    private BigDecimal expectedOdds;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

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
