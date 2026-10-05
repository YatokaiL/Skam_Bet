package ru.skambet.domain;

public enum BetStatus {
    PENDING("В игре"),
    WON("Выигрыш"),
    LOST("Проигрыш");

    private final String title;

    BetStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
