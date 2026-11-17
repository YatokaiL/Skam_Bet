package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.skambet.domain.BlockTerm;
import ru.skambet.domain.User;
import ru.skambet.service.BlacklistException;
import ru.skambet.service.BlacklistService;

import java.security.Principal;

/**
 * Feature 20: list of users with search, blocking and unblocking. Only for ADMIN, see SecurityConfig.
 */
@Controller
public class AdminUserController {

    private static final int MAX_QUERY_LENGTH = 50;

    private final BlacklistService blacklistService;

    public AdminUserController(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @GetMapping("/admin/users")
    public String users(@RequestParam(required = false) String q, Principal principal, Model model) {
        String query = q == null || q.isBlank() ? null : q.trim();
        if (query != null && query.length() > MAX_QUERY_LENGTH) {
            query = query.substring(0, MAX_QUERY_LENGTH);
        }
        model.addAttribute("query", query);
        model.addAttribute("users", blacklistService.findUsers(query));
        model.addAttribute("terms", BlockTerm.values());
        model.addAttribute("me", principal.getName());
        return "admin/users";
    }

    @PostMapping("/admin/users/{id}/block")
    public String block(@PathVariable Long id, @RequestParam(required = false) BlockTerm term,
                        @RequestParam(required = false) String reason, @RequestParam(required = false) String q,
                        Principal principal, RedirectAttributes redirect) {
        try {
            User user = blacklistService.block(principal.getName(), id, term, reason);
            redirect.addFlashAttribute("success",
                    "Пользователь " + user.getUsername() + " заблокирован " + BlacklistService.term(user));
        } catch (BlacklistException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return back(q, redirect);
    }

    @PostMapping("/admin/users/{id}/unblock")
    public String unblock(@PathVariable Long id, @RequestParam(required = false) String q,
                          RedirectAttributes redirect) {
        try {
            User user = blacklistService.unblock(id);
            redirect.addFlashAttribute("success", "Пользователь " + user.getUsername() + " разблокирован");
        } catch (BlacklistException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return back(q, redirect);
    }

    // the admin stays on the same search results
    private static String back(String q, RedirectAttributes redirect) {
        if (q != null && !q.isBlank()) {
            redirect.addAttribute("q", q.trim());
        }
        return "redirect:/admin/users";
    }
}
