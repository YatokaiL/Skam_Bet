package ru.skambet.web.form;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ru.skambet.service.UserService;

/**
 * Feature 9: checks for changing email and password in the personal account.
 * Rules for the new values are the same as at registration.
 */
@Component
public class ProfileValidator {

    private final UserService userService;

    public ProfileValidator(UserService userService) {
        this.userService = userService;
    }

    public void validateEmail(EmailForm form, String username, Errors errors) {
        String email = form.getEmail() == null ? "" : form.getEmail().trim();
        if (email.isEmpty()) {
            errors.rejectValue("email", "required", "Введите email");
        } else if (!RegistrationFormValidator.EMAIL.matcher(email).matches()) {
            errors.rejectValue("email", "invalid", "Введите корректный email");
        } else if (email.equalsIgnoreCase(userService.getByUsername(username).getEmail())) {
            errors.rejectValue("email", "same", "Новый email совпадает с текущим");
        } else if (userService.isEmailTaken(email)) {
            errors.rejectValue("email", "taken", "Пользователь с таким email уже существует");
        }
    }

    public void validatePassword(PasswordForm form, String username, Errors errors) {
        String current = form.getCurrentPassword() == null ? "" : form.getCurrentPassword();
        String password = form.getNewPassword() == null ? "" : form.getNewPassword();

        if (current.isEmpty()) {
            errors.rejectValue("currentPassword", "required", "Введите текущий пароль");
        } else if (!userService.isPasswordCorrect(username, current)) {
            errors.rejectValue("currentPassword", "wrong", "Неверный текущий пароль");
        }

        if (password.isEmpty()) {
            errors.rejectValue("newPassword", "required", "Введите новый пароль");
        } else if (password.length() < RegistrationFormValidator.MIN_PASSWORD_LENGTH) {
            errors.rejectValue("newPassword", "short", "Пароль должен содержать не менее 8 символов");
        } else if (password.length() > RegistrationFormValidator.MAX_PASSWORD_LENGTH) {
            errors.rejectValue("newPassword", "long", "Пароль должен содержать не более 64 символов");
        } else if (!password.equals(form.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "mismatch", "Пароли не совпадают");
        } else if (password.equals(current)) {
            errors.rejectValue("newPassword", "same", "Новый пароль должен отличаться от текущего");
        }
    }
}
