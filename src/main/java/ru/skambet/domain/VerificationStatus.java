package ru.skambet.domain;

/**
 * Feature 19: state of a request for verification of identity.
 */
public enum VerificationStatus {
    PENDING("На проверке"),
    APPROVED("Подтверждена"),
    REJECTED("Отклонена");

    private final String title;

    VerificationStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
