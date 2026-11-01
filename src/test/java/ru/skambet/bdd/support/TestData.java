package ru.skambet.bdd.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.skambet.domain.Bet;
import ru.skambet.domain.Event;
import ru.skambet.domain.ExpressBet;
import ru.skambet.domain.Outcome;
import ru.skambet.domain.Role;
import ru.skambet.domain.Sport;
import ru.skambet.domain.User;
import ru.skambet.repository.BetRepository;
import ru.skambet.repository.EventRepository;
import ru.skambet.repository.ExpressBetRepository;
import ru.skambet.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Prepares data directly in the database, so "Given" steps don't have to click through the UI.
 */
@Component
public class TestData {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final BetRepository betRepository;
    private final ExpressBetRepository expressBetRepository;

    public TestData(UserRepository userRepository, EventRepository eventRepository,
                    PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate,
                    BetRepository betRepository, ExpressBetRepository expressBetRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.betRepository = betRepository;
        this.expressBetRepository = expressBetRepository;
    }

    public void clear() {
        jdbcTemplate.execute("TRUNCATE express_legs, express_bets, withdrawals, bets, deposits, events, users "
                + "RESTART IDENTITY CASCADE");
    }

    public User createUser(String username, String password, BigDecimal balance) {
        User user = new User(username, username + "@test.ru", passwordEncoder.encode(password),
                LocalDate.of(1995, 6, 1));
        user.setBalance(balance);
        return userRepository.save(user);
    }

    public Event createEvent(Sport sport, String home, String away, LocalDateTime start,
                             BigDecimal oddsHome, BigDecimal oddsDraw, BigDecimal oddsAway) {
        return eventRepository.save(new Event(sport, home, away, start, oddsHome, oddsDraw, oddsAway));
    }

    /**
     * Balance as it is stored in the db, not as the page shows it.
     */
    public BigDecimal balanceInDb(String username) {
        return jdbcTemplate.queryForObject("SELECT balance FROM users WHERE username = ?", BigDecimal.class, username);
    }

    public int betsCountInDb(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bets b JOIN users u ON u.id = b.user_id WHERE u.username = ?",
                Integer.class, username);
        return count == null ? 0 : count;
    }

    // features 6-10

    public User createAdmin(String username, String password) {
        User admin = createUser(username, password, BigDecimal.ZERO);
        admin.setRole(Role.ADMIN);
        return userRepository.save(admin);
    }

    public Event eventByTitle(String title) {
        return eventRepository.findAll().stream()
                .filter(e -> e.getTitle().equals(title))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + title));
    }

    /**
     * Bet as if it was placed earlier: the balance is not changed, set it in the user step.
     */
    public Bet createBet(String username, String eventTitle, Outcome outcome, BigDecimal amount) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Event event = eventByTitle(eventTitle);
        return betRepository.save(new Bet(user, event, outcome, event.getOdds(outcome), amount));
    }

    /**
     * @param legs event title -> outcome, in the order of the coupon
     */
    public ExpressBet createExpress(String username, BigDecimal amount, Map<String, Outcome> legs) {
        User user = userRepository.findByUsername(username).orElseThrow();
        ExpressBet express = new ExpressBet(user, amount);
        legs.forEach((title, outcome) -> {
            Event event = eventByTitle(title);
            express.addLeg(event, outcome, event.getOdds(outcome));
        });
        return expressBetRepository.save(express);
    }

    public void moveEventToPast(String eventTitle) {
        jdbcTemplate.update("UPDATE events SET start_time = ? WHERE id = ?",
                LocalDateTime.now().minusMinutes(1), eventByTitle(eventTitle).getId());
    }

    public String betStatusInDb(String username, String eventTitle) {
        return jdbcTemplate.queryForObject(
                "SELECT b.status FROM bets b JOIN users u ON u.id = b.user_id "
                        + "WHERE u.username = ? AND b.event_id = ? ORDER BY b.id DESC LIMIT 1",
                String.class, username, eventByTitle(eventTitle).getId());
    }

    public String expressStatusInDb(String username) {
        return jdbcTemplate.queryForObject(
                "SELECT x.status FROM express_bets x JOIN users u ON u.id = x.user_id "
                        + "WHERE u.username = ? ORDER BY x.id DESC LIMIT 1",
                String.class, username);
    }

    public int expressCountInDb(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM express_bets x JOIN users u ON u.id = x.user_id WHERE u.username = ?",
                Integer.class, username);
        return count == null ? 0 : count;
    }

    public int withdrawalsCountInDb(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM withdrawals w JOIN users u ON u.id = w.user_id WHERE u.username = ?",
                Integer.class, username);
        return count == null ? 0 : count;
    }

    public String emailInDb(String username) {
        return jdbcTemplate.queryForObject("SELECT email FROM users WHERE username = ?", String.class, username);
    }
}
