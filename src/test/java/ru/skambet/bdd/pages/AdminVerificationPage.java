package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Feature 19: the administrator's list of documents waiting for review.
 */
public class AdminVerificationPage extends BasePage {

    public AdminVerificationPage(WebDriver driver) {
        super(driver);
    }

    public List<String> usernames() {
        visible(By.tagName("h1"));
        return texts(By.cssSelector("#requests-table .request-row .request-user"));
    }

    public String documentName(String username) {
        return row(username).findElement(By.className("document-link")).getText().trim();
    }

    public String documentUrl(String username) {
        return row(username).findElement(By.className("document-link")).getDomProperty("href");
    }

    public void approve(String username) {
        clickAndWaitForReload(row(username).findElement(By.xpath(".//button[normalize-space()='Подтвердить']")));
    }

    public void reject(String username, String reason) {
        WebElement row = row(username);
        WebElement input = row.findElement(By.name("reason"));
        input.clear();
        if (!reason.isEmpty()) {
            input.sendKeys(reason);
        }
        clickAndWaitForReload(row.findElement(By.xpath(".//button[normalize-space()='Отклонить']")));
    }

    private WebElement row(String username) {
        return visible(By.xpath("//tr[contains(@class,'request-row')][td[contains(@class,'request-user')]"
                + "[normalize-space()='" + username + "']]"));
    }
}
