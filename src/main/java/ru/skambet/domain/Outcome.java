package ru.skambet.domain;

public enum Outcome {
    HOME("П1"),
    DRAW("X"),
    AWAY("П2");

    private final String label;

    Outcome(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
