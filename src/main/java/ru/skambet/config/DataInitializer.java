package ru.skambet.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import java.time.temporal.ChronoUnit;

/**
 * Adds a demo account and some matches so the app is not empty after start.
 * Turned off in tests (app.demo-data=false).
 */
@Component
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, EventRepository eventRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("demo").isEmpty()) {
            User demo = new User("demo", "demo@skambet.ru", passwordEncoder.encode("demo12345"),
                    LocalDate.of(1999, 5, 20));
            demo.setBalance(new BigDecimal("5000.00"));
            userRepository.save(demo);
        }

        // db is persistent, old demo matches are in the past after a few days - add new ones then
        if (!eventRepository.findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now()).isEmpty()) {
            return;
        }
        LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        event(Sport.FOOTBALL, "Спартак", "ЦСКА", today.plusHours(3), "2.10", "3.40", "3.20");
        event(Sport.FOOTBALL, "Зенит", "Краснодар", today.plusHours(27), "1.65", "3.90", "5.20");
        event(Sport.FOOTBALL, "Локомотив", "Динамо", today.plusDays(2).plusHours(1), "2.35", "3.30", "2.95");
        event(Sport.HOCKEY, "СКА", "Ак Барс", today.plusHours(5), "1.95", "4.10", "3.05");
        event(Sport.HOCKEY, "Металлург Мг", "Авангард", today.plusDays(1).plusHours(6), "2.25", "4.00", "2.60");
        event(Sport.BASKETBALL, "ЦСКА", "Зенит", today.plusHours(8), "1.40", null, "2.85");
        event(Sport.BASKETBALL, "УНИКС", "Локомотив-Кубань", today.plusDays(3), "1.75", null, "2.05");
        event(Sport.TENNIS, "Медведев Д.", "Рублёв А.", today.plusHours(30), "1.55", null, "2.40");
        event(Sport.ESPORTS, "Team Spirit", "NAVI", today.plusHours(10), "1.80", null, "1.95");
    }

    private void event(Sport sport, String home, String away, LocalDateTime start,
                       String oddsHome, String oddsDraw, String oddsAway) {
        eventRepository.save(new Event(sport, home, away, start,
                new BigDecimal(oddsHome),
                oddsDraw == null ? null : new BigDecimal(oddsDraw),
                new BigDecimal(oddsAway)));
    }
}
