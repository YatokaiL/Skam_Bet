package ru.skambet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.skambet.bdd.support.TestData;
import ru.skambet.bdd.support.TestDatabaseConfig;
import ru.skambet.domain.Bet;
import ru.skambet.domain.Event;
import ru.skambet.domain.Outcome;
import ru.skambet.domain.Sport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Two requests that change the same balance at the same moment, on a real PostgreSQL.
 * Not a UI test: browser can't send two requests exactly at once.
 */
@SpringBootTest
@Import(TestDatabaseConfig.class)
@ActiveProfiles("test")
class BalanceConcurrencyTest {

    private static final String CARD = "4111111111111111";

    @Autowired
    private TestData data;
    @Autowired
    private BetService betService;
    @Autowired
    private DepositService depositService;
    @Autowired
    private SettlementService settlementService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        data.clear();
        data.createUser("ivan", "secret123", new BigDecimal("1000"));
    }

    @RepeatedTest(10)
    void cancelBetDoesNotLoseConcurrentDeposit() throws Exception {
        Bet bet = createBet(LocalDateTime.now().plusHours(5));

        parallel(() -> betService.cancelBet("ivan", bet.getId()),
                () -> depositService.deposit("ivan", new BigDecimal("500"), CARD));

        // 1000 + 200 back + 500 deposit
        assertThat(data.balanceInDb("ivan")).isEqualByComparingTo("1700");
    }

    @RepeatedTest(10)
    void doubleClickOnCancelReturnsMoneyOnce() throws Exception {
        Bet bet = createBet(LocalDateTime.now().plusHours(5));

        parallel(() -> betService.cancelBet("ivan", bet.getId()),
                () -> betService.cancelBet("ivan", bet.getId()));

        assertThat(data.balanceInDb("ivan")).isEqualByComparingTo("1200");
    }

    @RepeatedTest(10)
    void settlementDoesNotLoseConcurrentDeposit() throws Exception {
        Bet bet = createBet(LocalDateTime.now().minusHours(2));

        parallel(() -> settlementService.settle(bet.getEvent().getId(), Outcome.HOME),
                () -> depositService.deposit("ivan", new BigDecimal("500"), CARD));

        // 1000 + 200 * 2.10 win + 500 deposit
        assertThat(data.balanceInDb("ivan")).isEqualByComparingTo("1920");
    }

    @RepeatedTest(10)
    void eventIsSettledOnlyOnceByTwoAdministrators() throws Exception {
        Bet bet = createBet(LocalDateTime.now().minusHours(2));
        Long eventId = bet.getEvent().getId();

        parallel(() -> settlementService.settle(eventId, Outcome.HOME),
                () -> settlementService.settle(eventId, Outcome.AWAY));

        String result = jdbcTemplate.queryForObject("SELECT result FROM events", String.class);
        String status = jdbcTemplate.queryForObject("SELECT status FROM bets", String.class);
        if ("HOME".equals(result)) {
            assertThat(status).isEqualTo("WON");
            assertThat(data.balanceInDb("ivan")).isEqualByComparingTo("1420");
        } else {
            assertThat(result).isEqualTo("AWAY");
            assertThat(status).isEqualTo("LOST");
            assertThat(data.balanceInDb("ivan")).isEqualByComparingTo("1000");
        }
    }

    // bet on П1 2.10 for 200, the balance is not changed by TestData
    private Bet createBet(LocalDateTime eventStart) {
        Event event = data.createEvent(Sport.FOOTBALL, "Спартак", "ЦСКА", eventStart,
                new BigDecimal("2.10"), new BigDecimal("3.40"), new BigDecimal("3.20"));
        return data.createBet("ivan", event.getTitle(), Outcome.HOME, new BigDecimal("200"));
    }

    /**
     * Starts both actions at the same moment. The one that loses may be rejected
     * (bet already cancelled, event already settled) - that is the expected behaviour.
     */
    private static void parallel(Runnable first, Runnable second) throws Exception {
        CyclicBarrier start = new CyclicBarrier(2);
        AtomicReference<Throwable> unexpected = new AtomicReference<>();
        Thread a = new Thread(() -> runAfter(start, first, unexpected));
        Thread b = new Thread(() -> runAfter(start, second, unexpected));
        a.start();
        b.start();
        a.join();
        b.join();
        if (unexpected.get() != null) {
            throw new AssertionError("Unexpected error in a parallel request", unexpected.get());
        }
    }

    private static void runAfter(CyclicBarrier start, Runnable action, AtomicReference<Throwable> unexpected) {
        try {
            start.await();
            action.run();
        } catch (BetRejectedException | SettlementRejectedException e) {
            // second request of a double click
        } catch (Throwable e) {
            unexpected.compareAndSet(null, e);
        }
    }
}
