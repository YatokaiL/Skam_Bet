package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.skambet.domain.Event;
import ru.skambet.domain.Sport;
import ru.skambet.service.EventService;
import ru.skambet.service.LineException;
import ru.skambet.service.LineService;
import ru.skambet.web.form.EventForm;
import ru.skambet.web.form.EventFormValidator;
import ru.skambet.web.form.OddsForm;

import java.util.Optional;

/**
 * Feature 16: the administrator adds events to the line and changes odds. Only for ADMIN, see SecurityConfig.
 */
@Controller
public class AdminLineController {

    private final LineService lineService;
    private final EventService eventService;
    private final EventFormValidator validator;

    public AdminLineController(LineService lineService, EventService eventService, EventFormValidator validator) {
        this.lineService = lineService;
        this.eventService = eventService;
        this.validator = validator;
    }

    @GetMapping("/admin/line")
    public String linePage(Model model) {
        model.addAttribute("eventForm", new EventForm());
        fillPage(model);
        return "admin/line";
    }

    @PostMapping("/admin/line")
    public String addEvent(@ModelAttribute("eventForm") EventForm form, BindingResult result, Model model,
                           RedirectAttributes redirect) {
        validator.validateEvent(form, result);
        if (result.hasErrors()) {
            fillPage(model);
            return "admin/line";
        }
        Event event = lineService.addEvent(form.getSport(), form.getHomeTeam().trim(), form.getAwayTeam().trim(),
                form.parsedStartTime(), EventFormValidator.parse(form.getOddsHome()),
                EventFormValidator.parse(form.getOddsDraw()), EventFormValidator.parse(form.getOddsAway()));
        redirect.addFlashAttribute("success", "Событие «" + event.getTitle() + "» добавлено в линию");
        return "redirect:/admin/line";
    }

    @PostMapping("/admin/line/{id}/odds")
    public String changeOdds(@PathVariable Long id, @ModelAttribute OddsForm form, BindingResult result,
                             RedirectAttributes redirect) {
        Optional<Event> event = eventService.findById(id);
        if (event.isEmpty()) {
            redirect.addFlashAttribute("error", "Событие не найдено");
            return "redirect:/admin/line";
        }
        validator.validateOdds(form, event.get().getSport(), result);
        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", "Коэффициенты события «" + event.get().getTitle() + "» не изменены: "
                    + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/line";
        }
        try {
            lineService.changeOdds(id, EventFormValidator.parse(form.getOddsHome()),
                    EventFormValidator.parse(form.getOddsDraw()), EventFormValidator.parse(form.getOddsAway()));
            redirect.addFlashAttribute("success", "Коэффициенты события «" + event.get().getTitle() + "» обновлены");
        } catch (LineException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/line";
    }

    private void fillPage(Model model) {
        model.addAttribute("events", eventService.getLine(null));
        model.addAttribute("sports", Sport.values());
    }
}
