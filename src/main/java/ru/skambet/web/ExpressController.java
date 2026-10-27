package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.skambet.domain.Event;
import ru.skambet.domain.ExpressBet;
import ru.skambet.domain.Outcome;
import ru.skambet.service.BetRejectedException;
import ru.skambet.service.EventService;
import ru.skambet.service.ExpressService;
import ru.skambet.service.InsufficientFundsException;
import ru.skambet.web.form.Amounts;
import ru.skambet.web.form.ExpressForm;
import ru.skambet.web.form.ExpressFormValidator;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Feature 8: the line with odds on the left, express coupon on the right.
 */
@Controller
public class ExpressController {

    private final EventService eventService;
    private final ExpressService expressService;
    private final ExpressCoupon coupon;
    private final ExpressFormValidator expressValidator;

    public ExpressController(EventService eventService, ExpressService expressService, ExpressCoupon coupon,
                             ExpressFormValidator expressValidator) {
        this.eventService = eventService;
        this.expressService = expressService;
        this.coupon = coupon;
        this.expressValidator = expressValidator;
    }

    /**
     * Event of the coupon with the current odds, for the page.
     */
    public static class CouponItem {

        private final Event event;
        private final Outcome outcome;
        private final BigDecimal odds;

        public CouponItem(Event event, Outcome outcome, BigDecimal odds) {
            this.event = event;
            this.outcome = outcome;
            this.odds = odds;
        }

        public Event getEvent() {
            return event;
        }

        public Outcome getOutcome() {
            return outcome;
        }

        public BigDecimal getOdds() {
            return odds;
        }
    }

    @GetMapping("/express")
    public String expressPage(Model model) {
        model.addAttribute("expressForm", new ExpressForm());
        fillPage(model);
        return "express";
    }

    @PostMapping("/express/add")
    public String add(@RequestParam Long eventId, @RequestParam Outcome outcome, RedirectAttributes redirect) {
        Optional<Event> event = eventService.findById(eventId);
        if (event.isEmpty() || event.get().isStarted() || event.get().getOdds(outcome) == null) {
            redirect.addFlashAttribute("error", "Приём ставок на это событие закрыт");
        } else if (!coupon.add(eventId, outcome)) {
            redirect.addFlashAttribute("error",
                    "В экспресс можно добавить не более " + ExpressService.MAX_EVENTS + " событий");
        }
        return "redirect:/express";
    }

    @PostMapping("/express/remove")
    public String remove(@RequestParam Long eventId) {
        coupon.remove(eventId);
        return "redirect:/express";
    }

    @PostMapping("/express/clear")
    public String clear() {
        coupon.clear();
        return "redirect:/express";
    }

    @PostMapping("/express")
    public String placeExpress(@ModelAttribute("expressForm") ExpressForm form, BindingResult result,
                               Principal principal, Model model, RedirectAttributes redirect) {
        List<ExpressService.Selection> selections = coupon.getSelections();
        if (selections.size() < ExpressService.MIN_EVENTS) {
            model.addAttribute("error",
                    "Экспресс должен содержать не менее " + ExpressService.MIN_EVENTS + " событий");
            fillPage(model);
            return "express";
        }

        expressValidator.validate(form, result);
        if (!result.hasErrors()) {
            try {
                expressService.placeExpress(principal.getName(), selections, Amounts.parse(form.getAmount()));
                coupon.clear();
                redirect.addFlashAttribute("success", "Экспресс принят");
                return "redirect:/bets/my";
            } catch (InsufficientFundsException e) {
                result.rejectValue("amount", "insufficient", e.getMessage());
            } catch (BetRejectedException e) {
                // an event has started meanwhile - remove it, the user decides what to do next
                removeStartedEvents();
                model.addAttribute("error", e.getMessage());
            }
        }
        fillPage(model);
        return "express";
    }

    private void removeStartedEvents() {
        for (ExpressService.Selection s : coupon.getSelections()) {
            Optional<Event> event = eventService.findById(s.eventId());
            if (event.isEmpty() || event.get().isStarted()) {
                coupon.remove(s.eventId());
            }
        }
    }

    private void fillPage(Model model) {
        List<CouponItem> items = new ArrayList<>();
        List<BigDecimal> odds = new ArrayList<>();
        for (ExpressService.Selection s : coupon.getSelections()) {
            eventService.findById(s.eventId()).ifPresent(event -> {
                BigDecimal o = event.getOdds(s.outcome());
                items.add(new CouponItem(event, s.outcome(), o));
                odds.add(o);
            });
        }
        model.addAttribute("events", eventService.getLine(null));
        model.addAttribute("outcomes", Outcome.values());
        model.addAttribute("coupon", coupon);
        model.addAttribute("items", items);
        model.addAttribute("totalOdds", items.isEmpty() ? null : ExpressBet.totalOdds(odds));
    }
}
