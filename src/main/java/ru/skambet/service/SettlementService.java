package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.Bet;
import ru.skambet.domain.BetStatus;
import ru.skambet.domain.Event;
import ru.skambet.domain.ExpressBet;
import ru.skambet.domain.Outcome;
import ru.skambet.domain.User;
import ru.skambet.repository.BetRepository;
import ru.skambet.repository.EventRepository;
import ru.skambet.repository.ExpressBetRepository;
import ru.skambet.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Feature 6: administrator enters the result of a finished event,
 * bets on it become WON or LOST and winnings go to the balance.
 */
@Service
public class SettlementService {

    private final EventRepository eventRepository;
    private final BetRepository betRepository;
    private final ExpressBetRepository expressBetRepository;
    private final UserRepository userRepository;

    public SettlementService(EventRepository eventRepository, BetRepository betRepository,
                             ExpressBetRepository expressBetRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.betRepository = betRepository;
        this.expressBetRepository = expressBetRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsToSettle() {
        return eventRepository.findByStartTimeLessThanEqualAndResultIsNullOrderByStartTimeAsc(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Event> getSettledEvents() {
        return eventRepository.findByResultIsNotNullOrderBySettledAtDescIdDesc();
    }

    @Transactional
    public Event settle(Long eventId, Outcome result) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new SettlementRejectedException("Событие не найдено"));
        if (event.isSettled()) {
            throw new SettlementRejectedException("Событие уже рассчитано");
        }
        if (!event.isStarted()) {
            throw new SettlementRejectedException("Событие ещё не началось");
        }
        if (result == null || event.getOdds(result) == null) {
            throw new SettlementRejectedException("Недопустимый результат для этого события");
        }

        event.settle(result);

        for (Bet bet : betRepository.findByEventIdAndStatus(eventId, BetStatus.PENDING)) {
            bet.settle(result);
            if (bet.getStatus() == BetStatus.WON) {
                payOut(bet.getUser(), bet.getPotentialWin());
            }
        }

        for (ExpressBet express : expressBetRepository.findByEventAndStatus(eventId, BetStatus.PENDING)) {
            express.settle();
            if (express.getStatus() == BetStatus.WON) {
                payOut(express.getUser(), express.getPotentialWin());
            }
        }
        return event;
    }

    private void payOut(User owner, BigDecimal amount) {
        User user = userRepository.findForUpdate(owner.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + owner.getUsername()));
        user.deposit(amount);
    }
}
