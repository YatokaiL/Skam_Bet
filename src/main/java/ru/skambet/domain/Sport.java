package ru.skambet.domain;

import java.util.Arrays;
import java.util.Optional;

public enum Sport {
    FOOTBALL("Футбол", true),
    HOCKEY("Хоккей", true),
    BASKETBALL("Баскетбол", false),
    TENNIS("Теннис", false),
    ESPORTS("Киберспорт", false);

    private final String title;
    // feature 16: is there the odds on a draw (X)
    private final boolean drawPossible;

    Sport(String title, boolean drawPossible) {
        this.title = title;
        this.drawPossible = drawPossible;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDrawPossible() {
        return drawPossible;
    }

    public static Optional<Sport> byTitle(String title) {
        return Arrays.stream(values())
                .filter(s -> s.title.equalsIgnoreCase(title))
                .findFirst();
    }
}
