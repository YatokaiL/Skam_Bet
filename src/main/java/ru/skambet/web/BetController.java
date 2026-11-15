package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.skambet.domain.Bet;
import ru.skambet.domain.BetStatus;
import ru.skambet.domain.Event;
import ru.skambet.domain.ExpressBet;
import ru.skambet.domain.Outcome;
import ru.skambet.service.BetRejectedException;
import ru.skambet.service.BetService;
import ru.skambet.service.CashoutChangedException;
import ru.skambet.service.EventService;
import ru.skambet.service.ExpressService;
import ru.skambet.service.InsufficientFundsException;
import ru.skambet.service.OddsChangedException;
import ru.skambet.web.form.Amounts;
import ru.skambet.web.form.BetForm;
import ru.skambet.web.form.BetFormValidator;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class BetController {

    private static final String BETS_CLOSED = "Приём ставок на это событие закрыт";

    private final BetService betService;
    private final EventService eventService;
    private final BetFormValidator betValidator;
    private final ExpressService expressService;
    private final Formats formats;

    public BetController(BetService betService, EventService eventService, BetFormValidator betValidator,
                         ExpressService expressService, Formats formats) {
        this.betService = betService;
        this.eventService = eventService;
        this.betValidator = betValidator;
        this.expressService = expressService;
        this.formats = formats;
    }

    @GetMapping("/bets/new")
    public String betSlip(@RequestParam Long eventId, @RequestParam Outcome outcome,
                          Model model, RedirectAttributes redirect) {
        Optional<Event> event = eventService.findById(eventId);
        if (event.isEmpty() || !isBettable(event.get(), outcome)) {
            redirect.addFlashAttribute("error", BETS_CLOSED);
            return "redirect:/";
        }
        BetForm form = new BetForm();
        form.setEventId(eventId);
        form.setOutcome(outcome);
        model.addAttribute("betForm", form);
        fillSlip(model, event.get(), outcome);
        return "bets/new";
    }

    @PostMapping("/bets")
    public String placeBet(@ModelAttribute("betForm") BetForm form, BindingResult result,
                           Principal principal, Model model, RedirectAttributes redirect) {
        Optional<Event> event = form.getEventId() == null || form.getOutcome() == null
                ? Optional.empty()
                : eventService.findById(form.getEventId());
        if (event.isEmpty()) {
            redirect.addFlashAttribute("error", BETS_CLOSED);
            return "redirect:/";
        }

        betValidator.validate(form, result);
        if (!result.hasErrors()) {
            try {
                betService.placeBet(principal.getName(), form.getEventId(), form.getOutcome(),
                        Amounts.parse(form.getAmount()), form.getExpectedOdds());
                redirect.addFlashAttribute("success", "Ставка принята");
                return "redirect:/bets/my";
            } catch (InsufficientFundsException e) {
                result.rejectValue("amount", "insufficient", e.getMessage());
            } catch (OddsChangedException e) {
                // feature 16: the coupon is shown again with the new odds, the user confirms the bet
                model.addAttribute("oddsChanged", e.getMessage() + ". Проверьте купон и подтвердите ставку");
            } catch (BetRejectedException e) {
                redirect.addFlashAttribute("error", e.getMessage());
                return "redirect:/";
            }
        }
        fillSlip(model, event.get(), form.getOutcome());
        return "bets/new";
    }

    @GetMapping("/bets/my")
    public String myBets(@RequestParam(required = false) BetStatus status, Principal principal, Model model) {
        List<Bet> bets = betService.getUserBets(principal.getName());
        List<ExpressBet> expresses = expressService.getUserExpresses(principal.getName());

        // feature 14: counters of the tabs are over all bets, the tables and the summary - over the chosen tab
        Map<BetStatus, Long> counts = new EnumMap<>(BetStatus.class);
        for (BetStatus s : BetStatus.values()) {
            counts.put(s, bets.stream().filter(b -> b.getStatus() == s).count()
                    + expresses.stream().filter(x -> x.getStatus() == s).count());
        }
        List<Bet> shownBets = bets.stream().filter(b -> status == null || b.getStatus() == status).toList();
        List<ExpressBet> shownExpresses = expresses.stream()
                .filter(x -> status == null || x.getStatus() == status).toList();

        // cancelled and annulled bets were returned to the balance, so they are not counted as placed
        BigDecimal placed = BigDecimal.ZERO;
        BigDecimal won = BigDecimal.ZERO;
        for (Bet b : shownBets) {
            placed = isReturned(b.getStatus()) ? placed : placed.add(b.getAmount());
            won = b.getStatus() == BetStatus.WON ? won.add(b.getPotentialWin()) : won;
        }
        for (ExpressBet x : shownExpresses) {
            placed = isReturned(x.getStatus()) ? placed : placed.add(x.getAmount());
            won = x.getStatus() == BetStatus.WON ? won.add(x.getPotentialWin()) : won;
        }

        model.addAttribute("bets", shownBets);
        model.addAttribute("expresses", shownExpresses);
        model.addAttribute("statuses", BetStatus.values());
        model.addAttribute("counts", counts);
        model.addAttribute("totalCount", bets.size() + expresses.size());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("placed", placed);
        model.addAttribute("won", won);
        // feature 18
        model.addAttribute("cashoutOffers", betService.getCashoutOffers(principal.getName()));
        return "bets/my";
    }

    // feature 10
    @PostMapping("/bets/{id}/cancel")
    public String cancelBet(@PathVariable Long id, @RequestParam(required = false) BetStatus status,
                            Principal principal, RedirectAttributes redirect) {
        try {
            Bet bet = betService.cancelBet(principal.getName(), id);
            redirect.addFlashAttribute("success",
                    "Ставка отменена, " + formats.money(bet.getAmount()) + " возвращены на баланс");
        } catch (BetRejectedException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        // stay on the same status tab
        if (status != null) {
            redirect.addAttribute("status", status);
        }
        return "redirect:/bets/my";
    }

    /**
     * Feature 18: current cashout amounts, the page asks for them every few seconds (cashout.js).
     */
    @GetMapping("/bets/cashout-offers")
    @ResponseBody
    public Map<Long, String> cashoutOffers(Principal principal) {
        Map<Long, String> offers = new LinkedHashMap<>();
        betService.getCashoutOffers(principal.getName())
                .forEach((id, amount) -> offers.put(id, amount.toPlainString()));
        return offers;
    }

    @PostMapping("/bets/{id}/cashout")
    public String cashOut(@PathVariable Long id, @RequestParam(required = false) BigDecimal expectedAmount,
                          @RequestParam(required = false) BetStatus status, Principal principal,
                          RedirectAttributes redirect) {
        try {
            Bet bet = betService.cashOut(principal.getName(), id, expectedAmount);
            redirect.addFlashAttribute("success", "Ставка выкуплена за " + formats.money(bet.getCashoutAmount()));
        } catch (CashoutChangedException e) {
            redirect.addFlashAttribute("error", "Сумма выкупа изменилась: " + formats.money(e.getOldAmount())
                    + " → " + formats.money(e.getNewAmount()) + ". Проверьте и подтвердите выкуп");
        } catch (BetRejectedException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        if (status != null) {
            redirect.addAttribute("status", status);
        }
        return "redirect:/bets/my";
    }

    private static boolean isReturned(BetStatus status) {
        return status == BetStatus.CANCELLED || status == BetStatus.ANNULLED;
    }

    private static boolean isBettable(Event event, Outcome outcome) {
        return event.isOpenForBets() && event.getOdds(outcome) != null;
    }

    private static void fillSlip(Model model, Event event, Outcome outcome) {
        model.addAttribute("event", event);
        model.addAttribute("outcome", outcome);
        model.addAttribute("odds", event.getOdds(outcome));
    }
}
