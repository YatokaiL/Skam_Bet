package ru.skambet.web;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Used from templates as ${@fmt.money(...)}
 */
@Component("fmt")
public class Formats {

    public String money(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " ₽";
    }

    /**
     * Feature 15: "+500.00 ₽" for money to the balance, "-200.00 ₽" for money from it.
     */
    public String signedMoney(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return (value.signum() > 0 ? "+" : "") + money(value);
    }

    public String odds(BigDecimal value) {
        if (value == null) {
            return "—";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
