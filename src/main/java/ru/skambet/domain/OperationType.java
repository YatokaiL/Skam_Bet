package ru.skambet.domain;

/**
 * Feature 15: kinds of balance changes in the history of operations.
 */
public enum OperationType {
    DEPOSIT("Пополнение"),
    WITHDRAWAL("Вывод"),
    BET("Ставка"),
    EXPRESS("Экспресс"),
    WIN("Выигрыш"),
    REFUND("Возврат ставки");

    private final String title;

    OperationType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
