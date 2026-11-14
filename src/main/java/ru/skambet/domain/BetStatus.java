package ru.skambet.domain;

public enum BetStatus {
    PENDING("В игре"),
    WON("Выигрыш"),
    LOST("Проигрыш"),
    CANCELLED("Отменена"),
    // feature 17: the event was annulled, the money is returned
    ANNULLED("Аннулирована");

    private final String title;

    BetStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
