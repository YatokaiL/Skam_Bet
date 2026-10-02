package ru.skambet.web.form;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public final class Amounts {

    // "500", "500.5", "500,50" - up to 2 digits after the separator
    private static final Pattern AMOUNT = Pattern.compile("\\d{1,9}([.,]\\d{1,2})?");

    private Amounts() {
    }

    /**
     * @return parsed amount or null if the string is not a valid money value
     */
    public static BigDecimal parse(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replace(" ", "");
        if (!AMOUNT.matcher(normalized).matches()) {
            return null;
        }
        return new BigDecimal(normalized.replace(',', '.')).setScale(2, RoundingMode.UNNECESSARY);
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
