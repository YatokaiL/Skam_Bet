package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.skambet.domain.Event;
import ru.skambet.domain.Sport;
import ru.skambet.service.EventService;
import ru.skambet.service.FavoriteService;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Controller
public class HomeController {

    private final EventService eventService;
    private final FavoriteService favoriteService;

    public HomeController(EventService eventService, FavoriteService favoriteService) {
        this.eventService = eventService;
        this.favoriteService = favoriteService;
    }

    @GetMapping("/")
    public String line(@RequestParam(required = false) Sport sport, @RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "false") boolean favorites, Principal principal, Model model) {
        // feature 12: favorites exist only for a logged in user
        if (favorites && principal == null) {
            return "redirect:/login";
        }
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

        Set<Long> favoriteIds = principal == null ? Set.of() : favoriteService.getFavoriteEventIds(principal.getName());
        // the favorites tab shows favorite events of all sports
        Sport shownSport = favorites ? null : sport;
        List<Event> events = eventService.getLine(shownSport, appliedQuery);
        if (favorites) {
            events = events.stream().filter(e -> favoriteIds.contains(e.getId())).toList();
        }

        model.addAttribute("events", events);
        model.addAttribute("favoriteIds", favoriteIds);
        model.addAttribute("favoritesOnly", favorites);
        model.addAttribute("sports", Sport.values());
        model.addAttribute("selectedSport", shownSport);
        model.addAttribute("query", query);
        model.addAttribute("appliedQuery", appliedQuery);
        return "index";
    }
}
