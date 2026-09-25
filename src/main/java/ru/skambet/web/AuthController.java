package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.skambet.service.UserService;
import ru.skambet.web.form.RegistrationForm;
import ru.skambet.web.form.RegistrationFormValidator;

@Controller
public class AuthController {

    private final UserService userService;
    private final RegistrationFormValidator registrationValidator;

    public AuthController(UserService userService, RegistrationFormValidator registrationValidator) {
        this.userService = userService;
        this.registrationValidator = registrationValidator;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") RegistrationForm form, BindingResult result) {
        registrationValidator.validate(form, result);
        if (result.hasErrors()) {
            return "register";
        }
        userService.register(form.getUsername().trim(), form.getEmail().trim(),
                form.getPassword(), form.parsedBirthDate());
        return "redirect:/login?registered";
    }
}
