package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.Event;
import ru.skambet.domain.ExpressBet;
import ru.skambet.domain.OperationType;
import ru.skambet.domain.Outcome;
import ru.skambet.domain.User;
import ru.skambet.repository.EventRepository;
import ru.skambet.repository.ExpressBetRepository;
import ru.skambet.repository.UserRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Feature 8: express bet on 2-10 events.
 */
@Service
public class ExpressService {

    public static final int MIN_EVENTS = 2;
    public static final int MAX_EVENTS = 10;

    private final ExpressBetRepository expressBetRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OperationService operationService;

    public ExpressService(ExpressBetRepository expressBetRepository, EventRepository eventRepository,
                          UserRepository userRepository, OperationService operationService) {
        this.expressBetRepository = expressBetRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.operationService = operationService;
    }

    public record Selection(Long eventId, Outcome outcome) {
    }

    @Transactional
    public ExpressBet placeExpress(String username, List<Selection> selections, BigDecimal amount) {
        return placeExpress(username, selections, amount, null);
    }

    /**
     * @param expectedOdds total odds shown to the user in the coupon (feature 16), null - not checked
     */
    @Transactional
    public ExpressBet placeExpress(String username, List<Selection> selections, BigDecimal amount,
                                   BigDecimal expectedOdds) {
        if (selections.size() < MIN_EVENTS) {
            throw new BetRejectedException("Экспресс должен содержать не менее " + MIN_EVENTS + " событий");
        }
        if (selections.size() > MAX_EVENTS) {
            throw new BetRejectedException("В экспресс можно добавить не более " + MAX_EVENTS + " событий");
        }
        User user = userRepository.findForUpdate(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        ExpressBet express = new ExpressBet(user, amount);
        Set<Long> eventIds = new HashSet<>();
        for (Selection selection : selections) {
            Event event = eventRepository.findById(selection.eventId())
                    .orElseThrow(() -> new BetRejectedException("Событие не найдено"));
            if (!eventIds.add(event.getId())) {
                throw new BetRejectedException("В экспресс нельзя добавить два исхода одного события");
            }
            if (event.isStarted()) {
                throw new BetRejectedException("Приём ставок на событие «" + event.getTitle() + "» закрыт");
            }
            BigDecimal odds = event.getOdds(selection.outcome());
            if (odds == null) {
                throw new BetRejectedException("Ставка на этот исход недоступна");
            }
            express.addLeg(event, selection.outcome(), odds);
        }

        if (expectedOdds != null && express.getOdds().compareTo(expectedOdds) != 0) {
            throw new OddsChangedException(expectedOdds, express.getOdds());
        }
        if (user.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        user.withdraw(amount);
        ExpressBet saved = expressBetRepository.save(express);
        operationService.record(user, OperationType.EXPRESS, amount.negate(), describe(saved));
        return saved;
    }

    @Transactional(readOnly = true)
    public long countUserExpresses(String username) {
        return expressBetRepository.countByUserUsername(username);
    }

    @Transactional(readOnly = true)
    public List<ExpressBet> getUserExpresses(String username) {
        List<ExpressBet> list = expressBetRepository.findByUserUsernameOrderByPlacedAtDescIdDesc(username);
        // legs are lazy, the view needs them after the transaction (open-in-view is off)
        list.forEach(x -> x.getLegs().forEach(l -> l.getEvent().getTitle()));
        return list;
    }

    /**
     * "Экспресс из 2 событий" - for the history of operations.
     */
    public static String describe(ExpressBet express) {
        int n = express.getLegs().size();
        String events = n % 10 == 1 && n % 100 != 11 ? "события" : "событий";
        return "Экспресс из " + n + " " + events;
    }
}
