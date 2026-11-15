package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.Bet;
import ru.skambet.domain.BetStatus;
import ru.skambet.domain.Event;
import ru.skambet.domain.OperationType;
import ru.skambet.domain.Outcome;
import ru.skambet.domain.User;
import ru.skambet.repository.BetRepository;
import ru.skambet.repository.EventRepository;
import ru.skambet.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single bets. Every change of a bet locks the event first and the user second - the same order as
 * settlement and annulment use, so these operations wait for each other instead of mixing up.
 */
@Service
public class BetService {

    public static final BigDecimal MIN_AMOUNT = new BigDecimal("10");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("50000");
    // feature 18: the bookmaker keeps 10% when buying a bet back
    public static final BigDecimal CASHOUT_MARGIN = new BigDecimal("0.90");

    private final BetRepository betRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OperationService operationService;

    public BetService(BetRepository betRepository, EventRepository eventRepository, UserRepository userRepository,
                      OperationService operationService) {
        this.betRepository = betRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.operationService = operationService;
    }

    @Transactional
    public Bet placeBet(String username, Long eventId, Outcome outcome, BigDecimal amount) {
        return placeBet(username, eventId, outcome, amount, null);
    }

    /**
     * @param expectedOdds odds shown to the user in the coupon (feature 16), null - not checked
     */
    @Transactional
    public Bet placeBet(String username, Long eventId, Outcome outcome, BigDecimal amount,
                        BigDecimal expectedOdds) {
        Event event = eventRepository.findForUpdate(eventId)
                .orElseThrow(() -> new BetRejectedException("Событие не найдено"));
        User user = lockUser(username);

        if (event.isAnnulled()) {
            throw new BetRejectedException("Событие аннулировано");
        }
        if (event.isStarted()) {
            throw new BetRejectedException("Приём ставок на это событие закрыт");
        }
        BigDecimal odds = event.getOdds(outcome);
        if (odds == null) {
            throw new BetRejectedException("Ставка на этот исход недоступна");
        }
        if (expectedOdds != null && odds.compareTo(expectedOdds) != 0) {
            throw new OddsChangedException(expectedOdds, odds);
        }
        if (user.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        user.withdraw(amount);
        Bet bet = betRepository.save(new Bet(user, event, outcome, odds, amount));
        operationService.record(user, OperationType.BET, amount.negate(), describe(bet));
        return bet;
    }

    @Transactional(readOnly = true)
    public List<Bet> getUserBets(String username) {
        return betRepository.findByUserUsernameOrderByPlacedAtDescIdDesc(username);
    }

    @Transactional(readOnly = true)
    public long countUserBets(String username) {
        return betRepository.countByUserUsername(username);
    }

    /**
     * Feature 10: the bet is cancelled and the amount goes back to the balance.
     * Possible only while the event has not started.
     */
    @Transactional
    public Bet cancelBet(String username, Long betId) {
        Bet bet = lockBet(username, betId);
        if (bet.getStatus() != BetStatus.PENDING) {
            throw new BetRejectedException("Ставка уже рассчитана или отменена");
        }
        if (bet.getEvent().isStarted()) {
            throw new BetRejectedException("Отмена невозможна: событие уже началось");
        }
        User user = bet.getUser();
        user.deposit(bet.getAmount());
        bet.cancel();
        operationService.record(user, OperationType.REFUND, bet.getAmount(), "Отмена: " + describe(bet));
        return bet;
    }

    /**
     * Feature 18: the price the bookmaker pays for the bet now:
     * amount * odds of the bet / current odds * 0.90, rounded down to kopecks.
     * The more likely the outcome became (the odds fell), the more the bet is worth.
     *
     * @return null if the bet can't be bought back
     */
    public static BigDecimal cashoutOffer(Bet bet) {
        if (!bet.isCashable()) {
            return null;
        }
        BigDecimal currentOdds = bet.getEvent().getOdds(bet.getOutcome());
        return bet.getAmount().multiply(bet.getOdds()).multiply(CASHOUT_MARGIN)
                .divide(currentOdds, 2, RoundingMode.DOWN);
    }

    /**
     * Feature 18: offers for all bets of the user that can be bought back now, bet id -> amount.
     */
    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> getCashoutOffers(String username) {
        Map<Long, BigDecimal> offers = new LinkedHashMap<>();
        for (Bet bet : betRepository.findByUserUsernameOrderByPlacedAtDescIdDesc(username)) {
            BigDecimal offer = cashoutOffer(bet);
            if (offer != null) {
                offers.put(bet.getId(), offer);
            }
        }
        return offers;
    }

    /**
     * Feature 18: the bookmaker buys the bet back before the start of the event.
     *
     * @param expectedAmount the amount shown to the user, null - not checked
     */
    @Transactional
    public Bet cashOut(String username, Long betId, BigDecimal expectedAmount) {
        Bet bet = lockBet(username, betId);
        if (bet.getStatus() != BetStatus.PENDING) {
            throw new BetRejectedException("Ставка уже рассчитана, отменена или выкуплена");
        }
        if (!bet.getEvent().isOpenForBets()) {
            throw new BetRejectedException("Выкуп недоступен: событие уже началось");
        }
        BigDecimal offer = cashoutOffer(bet);
        if (offer == null) {
            throw new BetRejectedException("Выкуп этой ставки недоступен");
        }
        if (expectedAmount != null && offer.compareTo(expectedAmount) != 0) {
            throw new CashoutChangedException(expectedAmount, offer);
        }
        User user = bet.getUser();
        user.deposit(offer);
        bet.cashOut(offer);
        operationService.record(user, OperationType.CASHOUT, offer, "Выкуп: " + describe(bet));
        return bet;
    }

    /**
     * "Ставка П1 на «Спартак — ЦСКА»" - for the history of operations.
     */
    public static String describe(Bet bet) {
        return "Ставка " + bet.getOutcome().getLabel() + " на «" + bet.getEvent().getTitle() + "»";
    }

    /**
     * Locks the event of the bet, then the user, and only then reads the bet - so it is read
     * after settlement, annulment or another request of this user are finished.
     */
    private Bet lockBet(String username, Long betId) {
        Long eventId = betRepository.findEventId(betId)
                .orElseThrow(() -> new BetRejectedException("Ставка не найдена"));
        eventRepository.findForUpdate(eventId);
        lockUser(username);
        return betRepository.findById(betId)
                .filter(b -> b.getUser().getUsername().equals(username))
                .orElseThrow(() -> new BetRejectedException("Ставка не найдена"));
    }

    private User lockUser(String username) {
        return userRepository.findForUpdate(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
}
