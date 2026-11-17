package ru.skambet.domain;

import java.time.LocalDateTime;

/**
 * Feature 20: how long the user stays in the blacklist.
 */
public enum BlockTerm {

    DAY("1 день", 1),
    WEEK("7 дней", 7),
    MONTH("30 дней", 30),
    FOREVER("Навсегда", 0);

    private final String title;
    private final int days;

    BlockTerm(String title, int days) {
        this.title = title;
        this.days = days;
    }

    public String getTitle() {
        return title;
    }

    /**
     * End of the block, null - the block has no end.
     */
    public LocalDateTime until(LocalDateTime from) {
        return this == FOREVER ? null : from.plusDays(days);
    }
}
