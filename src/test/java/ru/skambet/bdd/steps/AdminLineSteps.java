package ru.skambet.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import ru.skambet.bdd.pages.AdminLinePage;
import ru.skambet.bdd.support.Browser;
import ru.skambet.bdd.support.TestData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature 16: line management by the administrator and changed odds in the coupon.
 */
public class AdminLineSteps {

    private static final DateTimeFormatter START = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final Pattern IN_HOURS = Pattern.compile("через (\\d+) ч");
    private static final Pattern HOURS_AGO = Pattern.compile("(\\d+) ч назад");

    private final Browser browser;
    private final TestData testData;

    public AdminLineSteps(Browser browser, TestData testData) {
        this.browser = browser;
        this.testData = testData;
    }

    /**
     * "начало" can be relative: "через 48 ч", "2 ч назад", or any text typed as it is.
     */
    @Когда("администратор заполняет новое событие:")
    public void fillEvent(DataTable table) {
        AdminLinePage page = adminLinePage();
        table.asMap(String.class, String.class).forEach((field, value) -> {
            String text = value == null ? "" : value;
            if (field.equals("вид спорта")) {
                page.selectSport(text);
            } else if (field.equals("начало")) {
                page.fill(field, startTime(text));
            } else {
                page.fill(field, text);
            }
        });
    }

    @Когда("администратор меняет коэффициенты события {string} на {string}, {string}, {string}")
    public void changeOdds(String event, String home, String draw, String away) {
        adminLinePage().changeOdds(event, home, draw.equals("-") ? null : draw, away);
    }

    @И("в управлении линией есть событие {string}")
    public void eventInAdminLine(String event) {
        assertThat(adminLinePage().eventTitles()).contains(event);
    }

    @Дано("коэффициент {string} события {string} изменился на {string}")
    public void oddsChanged(String outcome, String event, String odds) {
        testData.changeOdds(event, SettlementSteps.outcome(outcome), new BigDecimal(odds));
    }

    private AdminLinePage adminLinePage() {
        return new AdminLinePage(browser.driver());
    }

    private static String startTime(String text) {
        Matcher in = IN_HOURS.matcher(text);
        if (in.matches()) {
            return LocalDateTime.now().plusHours(Long.parseLong(in.group(1))).format(START);
        }
        Matcher ago = HOURS_AGO.matcher(text);
        if (ago.matches()) {
            return LocalDateTime.now().minusHours(Long.parseLong(ago.group(1))).format(START);
        }
        return text;
    }
}
