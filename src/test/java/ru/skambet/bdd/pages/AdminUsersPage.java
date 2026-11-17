package ru.skambet.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

/**
 * Feature 20: the administrator's list of users with blocking.
 */
public class AdminUsersPage extends BasePage {

    private static final By SEARCH = By.id("user-search");
    private static final By SEARCH_BUTTON = By.id("user-search-btn");
    private static final By USERNAMES = By.cssSelector("#users-table .user-row .user-name");

    public AdminUsersPage(WebDriver driver) {
        super(driver);
    }

    public void search(String query) {
        type(SEARCH, query);
        clickAndWaitForReload(SEARCH_BUTTON);
    }

    public List<String> usernames() {
        waitForHeading("Пользователи");
        return texts(USERNAMES);
    }

    public String status(String username) {
        return row(username).findElement(By.cssSelector(".user-status .badge")).getText().trim();
    }

    public String blockReason(String username) {
        return row(username).findElement(By.className("user-block-reason")).getText().trim();
    }

    public void block(String username, String term, String reason) {
        WebElement row = row(username);
        new Select(row.findElement(By.name("term"))).selectByVisibleText(term);
        WebElement input = row.findElement(By.name("reason"));
        input.clear();
        if (!reason.isEmpty()) {
            input.sendKeys(reason);
        }
        clickAndWaitForReload(row.findElement(By.xpath(".//button[normalize-space()='Заблокировать']")));
    }

    public void unblock(String username) {
        clickAndWaitForReload(row(username).findElement(By.xpath(".//button[normalize-space()='Разблокировать']")));
    }

    public boolean hasBlockButtons(String username) {
        return !row(username).findElements(By.xpath(".//button")).isEmpty();
    }

    public String actionNote(String username) {
        return row(username).findElement(By.className("user-no-block")).getText().trim();
    }

    private WebElement row(String username) {
        return visible(By.cssSelector("#users-table .user-row[data-username='" + username + "']"));
    }
}
