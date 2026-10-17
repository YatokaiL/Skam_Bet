package ru.skambet.bdd.steps;

import io.cucumber.java.ru.И;
import ru.skambet.bdd.support.TestData;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that what the page shows is really saved in the database.
 */
public class DatabaseSteps {

    private final TestData testData;

    public DatabaseSteps(TestData testData) {
        this.testData = testData;
    }

    @И("в базе данных баланс пользователя {string} равен {string}")
    public void balanceInDb(String username, String expected) {
        BigDecimal amount = new BigDecimal(expected.replace("₽", "").trim());
        assertThat(testData.balanceInDb(username))
                .as("balance of %s in db", username)
                .isEqualByComparingTo(amount);
    }

    @И("в базе данных нет ставок пользователя {string}")
    public void noBetsInDb(String username) {
        assertThat(testData.betsCountInDb(username)).as("bets of %s in db", username).isZero();
    }
}
