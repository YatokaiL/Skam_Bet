package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.Map;

/**
 * Feature 16: the administrator's page with the new event form and odds of the events in the line.
 */
public class AdminLinePage extends BasePage {

    // names of the fields as they are written in the feature files
    private static final Map<String, By> FIELDS = Map.of(
            "хозяева", By.id("homeTeam"),
            "гости", By.id("awayTeam"),
            "начало", By.id("startTime"),
            "П1", By.id("oddsHome"),
            "X", By.id("oddsDraw"),
            "П2", By.id("oddsAway"));

    public AdminLinePage(WebDriver driver) {
        super(driver);
    }

    public void selectSport(String sport) {
        new Select(visible(By.id("sport"))).selectByVisibleText(sport);
    }

    public void fill(String field, String value) {
        By locator = FIELDS.get(field);
        if (locator == null) {
            throw new IllegalArgumentException("Unknown field of the event form: " + field);
        }
        type(locator, value);
    }

    /**
     * @param draw null for sports without a draw
     */
    public void changeOdds(String eventTitle, String home, String draw, String away) {
        WebElement row = row(eventTitle);
        setValue(row.findElement(By.name("oddsHome")), home);
        if (draw != null) {
            setValue(row.findElement(By.name("oddsDraw")), draw);
        }
        setValue(row.findElement(By.name("oddsAway")), away);
        clickAndWaitForReload(row.findElement(By.xpath(".//button[normalize-space()='Сохранить']")));
    }

    public List<String> eventTitles() {
        visible(By.tagName("h1"));
        return texts(By.cssSelector("#line-table .line-row .event-title"));
    }

    private WebElement row(String eventTitle) {
        return visible(By.xpath("//tr[contains(@class,'line-row')][td[contains(@class,'event-title')]"
                + "[normalize-space()='" + eventTitle + "']]"));
    }

    private static void setValue(WebElement input, String value) {
        input.clear();
        input.sendKeys(value);
    }
}
