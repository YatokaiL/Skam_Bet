package ru.skambet.domain;

public enum BetStatus {
    PENDING("В игре"),
    WON("Выигрыш"),
    LOST("Проигрыш"),
    CANCELLED("Отменена"),
    // feature 17: the event was annulled, the money is returned
    ANNULLED("Аннулирована"),
    // feature 18: bought back by the bookmaker before the start
    CASHED_OUT("Выкуплена");

    private final String title;

    BetStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
