package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.skambet.service.BetService;
import ru.skambet.service.UserService;
import ru.skambet.web.form.EmailForm;
import ru.skambet.web.form.PasswordForm;
import ru.skambet.web.form.ProfileValidator;

import java.security.Principal;

/**
 * Feature 9: personal account - user data, change of email and password.
 */
@Controller
public class ProfileController {

    private final UserService userService;
    private final BetService betService;
    private final ProfileValidator profileValidator;

    public ProfileController(UserService userService, BetService betService, ProfileValidator profileValidator) {
        this.userService = userService;
        this.betService = betService;
        this.profileValidator = profileValidator;
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        model.addAttribute("emailForm", new EmailForm());
        model.addAttribute("passwordForm", new PasswordForm());
        fillProfile(principal, model);
        return "profile";
    }

    @PostMapping("/profile/email")
    public String changeEmail(@ModelAttribute("emailForm") EmailForm form, BindingResult result,
                              Principal principal, Model model, RedirectAttributes redirect) {
        profileValidator.validateEmail(form, principal.getName(), result);
        if (result.hasErrors()) {
            model.addAttribute("passwordForm", new PasswordForm());
            fillProfile(principal, model);
            return "profile";
        }
        userService.changeEmail(principal.getName(), form.getEmail().trim());
        redirect.addFlashAttribute("success", "Email изменён");
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@ModelAttribute("passwordForm") PasswordForm form, BindingResult result,
                                 Principal principal, Model model, RedirectAttributes redirect) {
        profileValidator.validatePassword(form, principal.getName(), result);
        if (result.hasErrors()) {
            model.addAttribute("emailForm", new EmailForm());
            fillProfile(principal, model);
            return "profile";
        }
        userService.changePassword(principal.getName(), form.getNewPassword());
        redirect.addFlashAttribute("success", "Пароль изменён");
        return "redirect:/profile";
    }

    private void fillProfile(Principal principal, Model model) {
        model.addAttribute("user", userService.getByUsername(principal.getName()));
        model.addAttribute("betsCount", betService.getUserBets(principal.getName()).size());
    }
}
