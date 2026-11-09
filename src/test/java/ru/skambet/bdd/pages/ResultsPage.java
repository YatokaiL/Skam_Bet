package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature 13: results of finished matches. Sport tabs are the same as in the line (see HomePage).
 */
public class ResultsPage extends BasePage {

    private static final By ROWS = By.cssSelector("#results-table .result-row");

    public ResultsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * @return rows of the table: событие, результат, коэффициент
     */
    public List<Map<String, String>> results() {
        visible(By.tagName("h1"));
        return driver.findElements(ROWS).stream().map(ResultsPage::toMap).toList();
    }

    private static Map<String, String> toMap(WebElement row) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("событие", row.findElement(By.className("event-title")).getText().trim());
        result.put("результат", row.findElement(By.className("result-outcome")).getText().trim());
        result.put("коэффициент", row.findElement(By.className("result-odds")).getText().trim());
        return result;
    }
}
