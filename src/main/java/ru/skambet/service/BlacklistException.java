package ru.skambet.service;

/**
 * Feature 20: the user can't be blocked or unblocked.
 */
public class BlacklistException extends RuntimeException {

    public BlacklistException(String message) {
        super(message);
    }
}
