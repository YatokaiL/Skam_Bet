package ru.skambet.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.skambet.domain.User;
import ru.skambet.repository.UserRepository;

import java.security.Principal;

/**
 * Puts logged in user into every model, header shows username and balance.
 */
@ControllerAdvice
public class CurrentUserAdvice {

    private final UserRepository userRepository;

    public CurrentUserAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("currentUser")
    public User currentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByUsername(principal.getName()).orElse(null);
    }
}
