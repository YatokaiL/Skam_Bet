package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.nio.file.Path;

/**
 * Feature 19: the user's page of verification of identity.
 */
public class VerificationPage extends BasePage {

    private static final By FILE_INPUT = By.id("document");

    public VerificationPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Selenium "types" the absolute path of the file into the file input - the browser uploads it.
     */
    public void attach(Path file) {
        visible(FILE_INPUT).sendKeys(file.toAbsolutePath().toString());
    }

    public void send() {
        submitWith("Отправить на проверку");
    }

    public void shouldShowStatus(String status) {
        waitForExactText(By.id("verification-status"), status, "Verification status");
    }

    public void shouldShowRejectReason(String reason) {
        waitForExactText(By.id("reject-reason"), reason, "Reject reason");
    }

    public boolean canSendDocument() {
        visible(By.tagName("h1"));
        return !driver.findElements(FILE_INPUT).isEmpty();
    }
}
