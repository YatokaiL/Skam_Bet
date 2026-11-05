package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.skambet.domain.Outcome;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature 8: line with odds buttons and the express coupon.
 * Amount and potential win have the same ids as in the single bet slip (see BetSlipPage).
 */
public class ExpressPage extends BasePage {

    private static final By LEGS = By.cssSelector("#express-coupon .express-leg");
    private static final By TOTAL_ODDS = By.id("coupon-odds");

    public ExpressPage(WebDriver driver) {
        super(driver);
    }

    public void addOutcome(String eventTitle, String outcomeLabel) {
        By button = By.xpath("//tr[contains(@class,'event-row')][td[contains(@class,'event-title')]"
                + "[normalize-space()='" + eventTitle + "']]//button[@data-outcome='" + outcomeCode(outcomeLabel) + "']");
        click(button);
        // page is reloaded, the chosen odds become highlighted
        wait.until(ExpectedConditions.attributeContains(button, "class", "odd--selected"));
    }

    public void removeEvent(String eventTitle) {
        int before = legs().size();
        click(By.xpath("//div[contains(@class,'express-leg')][.//b[normalize-space()='" + eventTitle + "']]"
                + "//button[normalize-space()='Убрать']"));
        wait.until(d -> d.findElements(LEGS).size() == before - 1);
    }

    /**
     * @return rows of the coupon: событие, исход, коэффициент
     */
    public List<Map<String, String>> legs() {
        visible(By.id("express-coupon"));
        return driver.findElements(LEGS).stream().map(ExpressPage::toMap).toList();
    }

    public String totalOdds() {
        return visible(TOTAL_ODDS).getText().trim();
    }

    private static Map<String, String> toMap(WebElement leg) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("событие", leg.findElement(By.className("express-leg__event")).getText().trim());
        map.put("исход", leg.findElement(By.className("express-leg__label")).getText().trim());
        map.put("коэффициент", leg.findElement(By.className("express-leg__odds")).getText().trim());
        return map;
    }

    private static String outcomeCode(String label) {
        return Arrays.stream(Outcome.values())
                .filter(o -> o.getLabel().equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown outcome: " + label))
                .name();
    }
}
