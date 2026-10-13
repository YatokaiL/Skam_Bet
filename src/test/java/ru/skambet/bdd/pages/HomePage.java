package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.skambet.domain.Outcome;
import ru.skambet.domain.Sport;

import java.util.Arrays;
import java.util.List;

/**
 * Main page with the line of events.
 */
public class HomePage extends BasePage {

    private static final By EVENT_TITLES = By.cssSelector(".event-row .event-title");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void selectSport(String title) {
        By tab = By.id("sport-" + sportCode(title));
        click(tab);
        wait.until(ExpectedConditions.attributeContains(tab, "class", "active"));
    }

    public String activeTab() {
        return visible(By.cssSelector(".sport-tabs .tab.active")).getText().trim();
    }

    public List<String> eventTitles() {
        visible(By.tagName("h1"));
        return texts(EVENT_TITLES);
    }

    public String odds(String eventTitle, String outcomeLabel) {
        return eventRow(eventTitle).findElement(outcomeLocator(outcomeLabel)).getText().trim();
    }

    public boolean isOutcomeClickable(String eventTitle, String outcomeLabel) {
        WebElement odd = eventRow(eventTitle).findElement(outcomeLocator(outcomeLabel));
        return odd.getTagName().equals("a");
    }

    public void chooseOutcome(String eventTitle, String outcomeLabel) {
        eventRow(eventTitle).findElement(outcomeLocator(outcomeLabel)).click();
    }

    private WebElement eventRow(String eventTitle) {
        By row = By.xpath("//tr[contains(@class,'event-row')][td[contains(@class,'event-title')][normalize-space()='"
                + eventTitle + "']]");
        return visible(row);
    }

    private static By outcomeLocator(String outcomeLabel) {
        Outcome outcome = Arrays.stream(Outcome.values())
                .filter(o -> o.getLabel().equals(outcomeLabel))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown outcome: " + outcomeLabel));
        return By.cssSelector(".odd[data-outcome='" + outcome.name() + "']");
    }

    private static String sportCode(String title) {
        if (title.equals("Все")) {
            return "ALL";
        }
        return Sport.byTitle(title)
                .orElseThrow(() -> new IllegalArgumentException("Unknown sport: " + title))
                .name();
    }
}
