package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.skambet.domain.Sport;
import ru.skambet.service.EventService;

@Controller
public class HomeController {

    private final EventService eventService;

    public HomeController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/")
    public String line(@RequestParam(required = false) Sport sport, @RequestParam(required = false) String q,
                       Model model) {
        String query = q == null || q.isBlank() ? null : q.trim();
        if (query != null && query.length() > EventService.MAX_QUERY_LENGTH) {
            query = query.substring(0, EventService.MAX_QUERY_LENGTH);
        }
        // feature 11: a too short query is not applied, the whole line is shown with a hint
        String appliedQuery = query;
        if (query != null && query.length() < EventService.MIN_QUERY_LENGTH) {
            model.addAttribute("searchError",
                    "Введите не менее " + EventService.MIN_QUERY_LENGTH + " символов для поиска");
            appliedQuery = null;
        }

        model.addAttribute("events", eventService.getLine(sport, appliedQuery));
        model.addAttribute("sports", Sport.values());
        model.addAttribute("selectedSport", sport);
        model.addAttribute("query", query);
        model.addAttribute("appliedQuery", appliedQuery);
        return "index";
    }
}
