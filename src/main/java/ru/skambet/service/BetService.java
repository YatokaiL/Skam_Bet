package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.Bet;
import ru.skambet.domain.Event;
import ru.skambet.domain.Outcome;
import ru.skambet.domain.User;
import ru.skambet.repository.BetRepository;
import ru.skambet.repository.EventRepository;
import ru.skambet.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BetService {

    public static final BigDecimal MIN_AMOUNT = new BigDecimal("10");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("50000");

    private final BetRepository betRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public BetService(BetRepository betRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.betRepository = betRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Bet placeBet(String username, Long eventId, Outcome outcome, BigDecimal amount) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BetRejectedException("Событие не найдено"));

        if (event.isStarted()) {
            throw new BetRejectedException("Приём ставок на это событие закрыт");
        }
        BigDecimal odds = event.getOdds(outcome);
        if (odds == null) {
            throw new BetRejectedException("Ставка на этот исход недоступна");
        }
        if (user.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        user.withdraw(amount);
        return betRepository.save(new Bet(user, event, outcome, odds, amount));
    }

    @Transactional(readOnly = true)
    public List<Bet> getUserBets(String username) {
        return betRepository.findByUserUsernameOrderByPlacedAtDescIdDesc(username);
    }
}
