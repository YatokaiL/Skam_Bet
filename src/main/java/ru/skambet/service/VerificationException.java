package ru.skambet.service;

/**
 * Feature 19: the document can't be accepted or the request can't be reviewed.
 */
public class VerificationException extends RuntimeException {

    public VerificationException(String message) {
        super(message);
    }
}
