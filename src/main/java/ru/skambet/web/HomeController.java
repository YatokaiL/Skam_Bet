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
    public String line(@RequestParam(required = false) Sport sport, Model model) {
        model.addAttribute("events", eventService.getLine(sport));
        model.addAttribute("sports", Sport.values());
        model.addAttribute("selectedSport", sport);
        return "index";
    }
}
