package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.skambet.domain.Sport;
import ru.skambet.service.EventService;

/**
 * Feature 13: results of finished matches, available without login.
 */
@Controller
public class ResultsController {

    private final EventService eventService;

    public ResultsController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/results")
    public String results(@RequestParam(required = false) Sport sport, Model model) {
        model.addAttribute("events", eventService.getResults(sport));
        model.addAttribute("sports", Sport.values());
        model.addAttribute("selectedSport", sport);
        return "results";
    }
}
