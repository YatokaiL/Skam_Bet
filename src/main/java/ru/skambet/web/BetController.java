package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.skambet.domain.Bet;
import ru.skambet.domain.Event;
import ru.skambet.domain.Outcome;
import ru.skambet.service.BetRejectedException;
import ru.skambet.service.BetService;
import ru.skambet.service.EventService;
import ru.skambet.service.ExpressService;
import ru.skambet.service.InsufficientFundsException;
import ru.skambet.web.form.Amounts;
import ru.skambet.web.form.BetForm;
import ru.skambet.web.form.BetFormValidator;

import java.security.Principal;
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
                        Amounts.parse(form.getAmount()));
                redirect.addFlashAttribute("success", "Ставка принята");
                return "redirect:/bets/my";
            } catch (InsufficientFundsException e) {
                result.rejectValue("amount", "insufficient", e.getMessage());
            } catch (BetRejectedException e) {
                redirect.addFlashAttribute("error", e.getMessage());
                return "redirect:/";
            }
        }
        fillSlip(model, event.get(), form.getOutcome());
        return "bets/new";
    }

    @GetMapping("/bets/my")
    public String myBets(Principal principal, Model model) {
        model.addAttribute("bets", betService.getUserBets(principal.getName()));
        model.addAttribute("expresses", expressService.getUserExpresses(principal.getName()));
        return "bets/my";
    }

    // feature 10
    @PostMapping("/bets/{id}/cancel")
    public String cancelBet(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        try {
            Bet bet = betService.cancelBet(principal.getName(), id);
            redirect.addFlashAttribute("success",
                    "Ставка отменена, " + formats.money(bet.getAmount()) + " возвращены на баланс");
        } catch (BetRejectedException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bets/my";
    }

    private static boolean isBettable(Event event, Outcome outcome) {
        return !event.isStarted() && event.getOdds(outcome) != null;
    }

    private static void fillSlip(Model model, Event event, Outcome outcome) {
        model.addAttribute("event", event);
        model.addAttribute("outcome", outcome);
        model.addAttribute("odds", event.getOdds(outcome));
    }
}
