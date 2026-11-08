package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.skambet.domain.Event;
import ru.skambet.domain.Sport;
import ru.skambet.service.EventService;
import ru.skambet.service.FavoriteService;

import java.security.Principal;
import java.util.Optional;

/**
 * Feature 12: the star in the line adds an event to the favorites or removes it.
 */
@Controller
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final EventService eventService;

    public FavoriteController(FavoriteService favoriteService, EventService eventService) {
        this.favoriteService = favoriteService;
        this.eventService = eventService;
    }

    @PostMapping("/favorites/{eventId}")
    public String toggle(@PathVariable Long eventId, @RequestParam(required = false) Sport sport,
                         @RequestParam(required = false) String q, @RequestParam(defaultValue = "false") boolean favorites,
                         Principal principal, RedirectAttributes redirect) {
        Optional<Event> event = eventService.findById(eventId);
        if (event.isEmpty()) {
            redirect.addFlashAttribute("error", "Событие не найдено");
        } else {
            boolean added = favoriteService.toggle(principal.getName(), eventId);
            redirect.addFlashAttribute("success", "Событие «" + event.get().getTitle() + "» "
                    + (added ? "добавлено в избранное" : "удалено из избранного"));
        }
        // back to the same view of the line
        if (sport != null) {
            redirect.addAttribute("sport", sport);
        }
        if (q != null && !q.isBlank()) {
            redirect.addAttribute("q", q);
        }
        if (favorites) {
            redirect.addAttribute("favorites", true);
        }
        return "redirect:/";
    }
}
