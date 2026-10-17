package ru.skambet.bdd.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.skambet.domain.Event;
import ru.skambet.domain.Sport;
import ru.skambet.domain.User;
import ru.skambet.repository.EventRepository;
import ru.skambet.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Prepares data directly in the database, so "Given" steps don't have to click through the UI.
 */
@Component
public class TestData {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public TestData(UserRepository userRepository, EventRepository eventRepository,
                    PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clear() {
        jdbcTemplate.execute("TRUNCATE bets, deposits, events, users RESTART IDENTITY CASCADE");
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
}
