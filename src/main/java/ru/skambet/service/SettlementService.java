package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.Bet;
import ru.skambet.domain.BetStatus;
import ru.skambet.domain.Event;
import ru.skambet.domain.OperationType;
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
    private final OperationService operationService;

    public SettlementService(EventRepository eventRepository, BetRepository betRepository,
                             ExpressBetRepository expressBetRepository, UserRepository userRepository,
                             OperationService operationService) {
        this.eventRepository = eventRepository;
        this.betRepository = betRepository;
        this.expressBetRepository = expressBetRepository;
        this.userRepository = userRepository;
        this.operationService = operationService;
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsToSettle() {
        return eventRepository.findByStartTimeLessThanEqualAndResultIsNullAndAnnulledAtIsNullOrderByStartTimeAsc(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Event> getSettledEvents() {
        return eventRepository.findBySettledAtIsNotNullOrderBySettledAtDescIdDesc();
    }

    @Transactional
    public Event settle(Long eventId, Outcome result) {
        Event event = eventRepository.findForUpdate(eventId)
                .orElseThrow(() -> new SettlementRejectedException("Событие не найдено"));
        if (event.isAnnulled()) {
            throw new SettlementRejectedException("Событие аннулировано");
        }
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
                payOut(bet.getUser(), bet.getPotentialWin(), "Выигрыш: " + BetService.describe(bet));
            }
        }

        for (ExpressBet express : expressBetRepository.findByEventAndStatus(eventId, BetStatus.PENDING)) {
            express.settle();
            if (express.getStatus() == BetStatus.WON) {
                payOut(express.getUser(), express.getPotentialWin(),
                        "Выигрыш: " + ExpressService.describe(express));
            }
        }
        return event;
    }

    /**
     * @param returnedBets single bets and whole expresses whose amount was returned
     */
    public record AnnulResult(Event event, int returnedBets) {
    }

    /**
     * Feature 17: the match is cancelled or postponed. Single bets are returned, in expresses the event
     * counts with the odds 1.00 - the express is recalculated, and wins at once if the other events are guessed.
     */
    @Transactional
    public AnnulResult annul(Long eventId, String reason) {
        Event event = eventRepository.findForUpdate(eventId)
                .orElseThrow(() -> new SettlementRejectedException("Событие не найдено"));
        if (event.isAnnulled()) {
            throw new SettlementRejectedException("Событие уже аннулировано");
        }
        if (event.isSettled()) {
            throw new SettlementRejectedException("Событие уже рассчитано, аннулировать его нельзя");
        }

        event.annul(reason);
        int returned = 0;

        for (Bet bet : betRepository.findByEventIdAndStatus(eventId, BetStatus.PENDING)) {
            bet.annul();
            refund(bet.getUser(), bet.getAmount(), "Аннулирование: " + BetService.describe(bet));
            returned++;
        }

        for (ExpressBet express : expressBetRepository.findByEventAndStatus(eventId, BetStatus.PENDING)) {
            express.recalculateAfterAnnulment();
            if (express.getStatus() == BetStatus.ANNULLED) {
                refund(express.getUser(), express.getAmount(), "Аннулирование: " + ExpressService.describe(express));
                returned++;
                continue;
            }
            // the annulled event could be the last one the express was waiting for
            express.settle();
            if (express.getStatus() == BetStatus.WON) {
                payOut(express.getUser(), express.getPotentialWin(),
                        "Выигрыш: " + ExpressService.describe(express));
            }
        }
        return new AnnulResult(event, returned);
    }

    // users were loaded together with the bets, without a lock - so the balance is changed in the db directly
    private void payOut(User owner, BigDecimal amount, String description) {
        userRepository.addToBalance(owner.getId(), amount);
        operationService.recordAfterUpdate(owner, OperationType.WIN, amount, description);
    }

    private void refund(User owner, BigDecimal amount, String description) {
        userRepository.addToBalance(owner.getId(), amount);
        operationService.recordAfterUpdate(owner, OperationType.REFUND, amount, description);
    }
}
