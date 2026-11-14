package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Feature 17: the annulment form in a row of an event - on the settlement and line management pages.
 */
public class AnnulmentForm extends BasePage {

    public AnnulmentForm(WebDriver driver) {
        super(driver);
    }

    public void annul(String eventTitle, String reason) {
        WebElement row = visible(By.xpath("//tr[td[contains(@class,'event-title')][normalize-space()='"
                + eventTitle + "']][.//form[contains(@class,'annul-form')]]"));
        WebElement input = row.findElement(By.name("reason"));
        input.clear();
        if (!reason.isEmpty()) {
            input.sendKeys(reason);
        }
        clickAndWaitForReload(row.findElement(By.xpath(".//button[normalize-space()='Аннулировать']")));
    }
}
