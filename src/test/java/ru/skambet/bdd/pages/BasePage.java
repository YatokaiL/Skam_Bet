package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Common things for every page: waits, header with user info, alerts and errors.
 */
public abstract class BasePage {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static final By HEADING = By.tagName("h1");
    private static final By CURRENT_USER = By.id("current-user");
    private static final By BALANCE = By.id("balance");
    private static final By MESSAGES = By.cssSelector(".alert-success, .empty-state");
    private static final By ERRORS = By.cssSelector(".field-error, .alert-error");

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TIMEOUT);
        // the page can be reloaded right while we check it. An old element then gives StaleElementReference,
        // chrome 156+ gives "Node with given id does not belong to the document" instead - just look again
        this.wait.ignoring(StaleElementReferenceException.class);
        this.wait.ignoring(WebDriverException.class);
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement input = visible(locator);
        input.clear();
        if (text != null && !text.isEmpty()) {
            input.sendKeys(text);
        }
    }

    /**
     * Clicks and waits until the browser has left the current page (a form is sent, a link is followed).
     */
    protected void clickAndWaitForReload(By locator) {
        clickAndWaitForReload(wait.until(ExpectedConditions.elementToBeClickable(locator)));
    }

    protected void clickAndWaitForReload(WebElement element) {
        WebElement html = driver.findElement(By.tagName("html"));
        element.click();
        wait.until(d -> {
            try {
                html.isEnabled();
                return false;
            } catch (WebDriverException e) {
                // stale element, or "node does not belong to the document" in chrome 156+ - the page is new
                return true;
            }
        });
    }

    protected List<String> texts(By locator) {
        return driver.findElements(locator).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .toList();
    }

    /**
     * Waits until the element has exactly this text, otherwise fails with the actual value.
     */
    protected void waitForExactText(By locator, String expected, String description) {
        try {
            wait.until(ExpectedConditions.textToBe(locator, expected));
        } catch (TimeoutException e) {
            throw new AssertionError(description + ": expected '" + expected + "' but was " + texts(locator));
        }
    }

    /**
     * Waits until at least one of the elements contains the text.
     */
    protected void waitForAnyContaining(By locator, String expected, String description) {
        try {
            wait.until(d -> d.findElements(locator).stream()
                    .anyMatch(e -> e.isDisplayed() && e.getText().contains(expected)));
        } catch (TimeoutException e) {
            throw new AssertionError(description + " '" + expected + "' not found, on page: " + texts(locator));
        }
    }

    public void waitForHeading(String heading) {
        waitForExactText(HEADING, heading, "Page heading");
    }

    public String heading() {
        return visible(HEADING).getText().trim();
    }

    public void clickButton(String text) {
        click(By.xpath("//button[normalize-space()='" + text + "']"));
    }

    /**
     * Sends the form with this button and waits for the next page, so the next step works with the new page.
     */
    public void submitWith(String buttonText) {
        clickAndWaitForReload(By.xpath("//button[normalize-space()='" + buttonText + "']"));
    }

    public void clickNavLink(String text) {
        click(By.xpath("//nav//a[normalize-space()='" + text + "']"));
    }

    public void shouldShowMessage(String message) {
        waitForAnyContaining(MESSAGES, message, "Message");
    }

    public void shouldShowError(String error) {
        waitForAnyContaining(ERRORS, error, "Error");
    }

    public void shouldShowCurrentUser(String username) {
        waitForExactText(CURRENT_USER, username, "User in header");
    }

    public void shouldShowBalance(String balance) {
        waitForExactText(BALANCE, balance, "Balance in header");
    }

    public void shouldShowHeaderLink(String text) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//header//a[normalize-space()='" + text + "']")));
        } catch (TimeoutException e) {
            throw new AssertionError("Link '" + text + "' is not shown in header");
        }
    }

    public boolean isLoggedIn() {
        return !driver.findElements(CURRENT_USER).isEmpty();
    }
}
