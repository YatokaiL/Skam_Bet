package ru.skambet.bdd.steps;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.skambet.bdd.pages.AdminVerificationPage;
import ru.skambet.bdd.pages.VerificationPage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;
import ru.skambet.bdd.support.TestFiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 19: verification of identity.
 */
public class VerificationSteps {

    private final Browser browser;
    private final TestData testData;

    public VerificationSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    @Дано("пользователь {string} прошёл верификацию")
    public void userIsVerified(String username) {
        testData.verifyUser(username);
    }

    @Дано("пользователь {string} отправил на проверку документ {string}")
    public void userSentDocument(String username, String fileName) {
        testData.createVerificationRequest(username, fileName);
    }

    /**
     * File name from src/test/resources/documents, or "файл размером 6 МБ" - it is made right in the test.
     */
    @Когда("пользователь прикрепляет файл {string}")
    public void attach(String fileName) {
        verificationPage().attach(TestFiles.document(fileName));
    }

    @Когда("пользователь отправляет документ на проверку")
    public void send() {
        verificationPage().send();
    }

    @Тогда("статус верификации {string}")
    public void status(String status) {
        verificationPage().shouldShowStatus(status);
    }

    @И("причина отклонения {string}")
    public void rejectReason(String reason) {
        verificationPage().shouldShowRejectReason(reason);
    }

    @И("форма отправки документа доступна")
    public void formAvailable() {
        assertThat(verificationPage().canSendDocument()).as("document form").isTrue();
    }

    @И("форма отправки документа недоступна")
    public void formNotAvailable() {
        assertThat(verificationPage().canSendDocument()).as("document form").isFalse();
    }

    @Тогда("на проверке документ {string} пользователя {string}")
    public void requestIsWaiting(String fileName, String username) {
        AdminVerificationPage page = adminPage();
        assertThat(page.usernames()).contains(username);
        assertThat(page.documentName(username)).isEqualTo(fileName);
    }

    /**
     * The link opens the document itself - checked with the cookies of the browser session.
     */
    @И("документ пользователя {string} открывается как {string}")
    public void documentOpens(String username, String contentType) throws Exception {
        String url = adminPage().documentUrl(username);
        String session = browser.driver().manage().getCookieNamed("JSESSIONID").getValue();
        HttpResponse<byte[]> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url)).header("Cookie", "JSESSIONID=" + session).build(),
                HttpResponse.BodyHandlers.ofByteArray());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type")).hasValue(contentType);
        assertThat(response.body()).isNotEmpty();
    }

    @Когда("администратор подтверждает документ пользователя {string}")
    public void approve(String username) {
        adminPage().approve(username);
    }

    @Когда("администратор отклоняет документ пользователя {string} с причиной {string}")
    public void reject(String username, String reason) {
        adminPage().reject(username, reason);
    }

    private VerificationPage verificationPage() {
        return new VerificationPage(browser.driver());
    }

    private AdminVerificationPage adminPage() {
        return new AdminVerificationPage(browser.driver());
    }
}
