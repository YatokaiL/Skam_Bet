package ru.skambet.domain;

import java.util.Arrays;
import java.util.Optional;

public enum Sport {
    FOOTBALL("Футбол"),
    HOCKEY("Хоккей"),
    BASKETBALL("Баскетбол"),
    TENNIS("Теннис"),
    ESPORTS("Киберспорт");

    private final String title;

    Sport(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static Optional<Sport> byTitle(String title) {
        return Arrays.stream(values())
                .filter(s -> s.title.equalsIgnoreCase(title))
                .findFirst();
    }
}
