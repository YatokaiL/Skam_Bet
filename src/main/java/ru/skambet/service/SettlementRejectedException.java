package ru.skambet.service;

public class SettlementRejectedException extends RuntimeException {

    public SettlementRejectedException(String message) {
        super(message);
    }
}
