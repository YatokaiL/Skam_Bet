package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature 15: history of operations on the balance.
 */
public class OperationsPage extends BasePage {

    private static final By ROWS = By.cssSelector("#operations-table .operation-row");

    public OperationsPage(WebDriver driver) {
        super(driver);
    }

    public void selectType(String title) {
        By tab = By.xpath("//div[@id='type-tabs']/a[normalize-space()='" + title + "']");
        clickAndWaitForReload(tab);
        wait.until(ExpectedConditions.attributeContains(tab, "class", "active"));
    }

    /**
     * @return rows of the table: операция, описание, сумма, баланс после
     */
    public List<Map<String, String>> operations() {
        visible(By.tagName("h1"));
        return driver.findElements(ROWS).stream().map(OperationsPage::toMap).toList();
    }

    private static Map<String, String> toMap(WebElement row) {
        Map<String, String> operation = new LinkedHashMap<>();
        operation.put("операция", row.findElement(By.className("operation-type")).getText().trim());
        operation.put("описание", row.findElement(By.className("operation-description")).getText().trim());
        operation.put("сумма", row.findElement(By.className("operation-amount")).getText().trim());
        operation.put("баланс после", row.findElement(By.className("operation-balance")).getText().trim());
        return operation;
    }
}
