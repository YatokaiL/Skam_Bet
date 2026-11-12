package ru.skambet.service;

/**
 * Feature 16: the line can't be changed (event not found or already started).
 */
public class LineException extends RuntimeException {

    public LineException(String message) {
        super(message);
    }
}
