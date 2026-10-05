package ru.skambet.service;

public class BetRejectedException extends RuntimeException {

    public BetRejectedException(String message) {
        super(message);
    }
}
