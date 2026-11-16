package ru.skambet.service;

/**
 * Feature 19: withdrawals are possible only after the verification of identity.
 */
public class VerificationRequiredException extends RuntimeException {

    public VerificationRequiredException() {
        super("Вывод средств доступен только после верификации личности");
    }
}
