package ru.skambet.service;

public class InsufficientFundsException extends BetRejectedException {

    public InsufficientFundsException() {
        super("Недостаточно средств на балансе");
    }
}
