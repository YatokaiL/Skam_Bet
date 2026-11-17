package ru.skambet.bdd.steps;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.AdminUsersPage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 20: blacklist.
 */
public class BlacklistSteps {

    private final Browser browser;
    private final TestData testData;

    public BlacklistSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    /**
     * The block is made by another administrator, while this browser keeps its own session.
     */
    @Дано("пользователь {string} заблокирован навсегда с причиной {string}")
    public void blockedForever(String username, String reason) {
        testData.blockUser(username, null, reason);
    }

    @Дано("пользователь {string} заблокирован на {int} ч с причиной {string}")
    public void blockedForHours(String username, int hours, String reason) {
        testData.blockUser(username, hours, reason);
    }

    @Дано("блокировка пользователя {string} с причиной {string} закончилась {int} ч назад")
    public void blockExpired(String username, String reason, int hoursAgo) {
        testData.blockExpired(username, hoursAgo, reason);
    }

    @Когда("администратор ищет пользователя {string}")
    public void search(String query) {
        page().search(query);
    }

    @Тогда("в списке пользователей:")
    public void usersAre(List<String> usernames) {
        assertThat(page().usernames()).containsExactlyElementsOf(usernames);
    }

    @Когда("администратор блокирует пользователя {string} на срок {string} с причиной {string}")
    public void block(String username, String term, String reason) {
        page().block(username, term, reason);
    }

    @Когда("администратор разблокирует пользователя {string}")
    public void unblock(String username) {
        page().unblock(username);
    }

    @Тогда("статус пользователя {string} — {string}")
    public void status(String username, String status) {
        assertThat(page().status(username)).isEqualTo(status);
    }

    /**
     * For a block for a term - the end date depends on the time of the run.
     */
    @Тогда("статус пользователя {string} начинается с {string}")
    public void statusStartsWith(String username, String start) {
        assertThat(page().status(username)).startsWith(start);
    }

    @И("причина блокировки пользователя {string} — {string}")
    public void reason(String username, String reason) {
        assertThat(page().blockReason(username)).isEqualTo(reason);
    }

    @Тогда("пользователя {string} нельзя заблокировать, указано {string}")
    public void cannotBlock(String username, String note) {
        AdminUsersPage page = page();
        assertThat(page.hasBlockButtons(username)).as("block buttons for " + username).isFalse();
        assertThat(page.actionNote(username)).isEqualTo(note);
    }

    @И("в базе данных пользователь {string} заблокирован на {int} дн. с причиной {string}")
    public void blockedInDbForDays(String username, int days, String reason) {
        assertThat(testData.blockDaysInDb(username)).as("days of the block").isEqualTo(days);
        assertThat(testData.blockReasonInDb(username)).isEqualTo(reason);
    }

    @И("в базе данных пользователь {string} заблокирован навсегда с причиной {string}")
    public void blockedInDbForever(String username, String reason) {
        assertThat(testData.blockDaysInDb(username)).as("block forever").isEqualTo(-1);
        assertThat(testData.blockReasonInDb(username)).isEqualTo(reason);
    }

    @И("в базе данных пользователь {string} не заблокирован")
    public void notBlockedInDb(String username) {
        assertThat(testData.blockDaysInDb(username)).as("days of the block").isNull();
    }

    private AdminUsersPage page() {
        return new AdminUsersPage(browser.driver());
    }
}
