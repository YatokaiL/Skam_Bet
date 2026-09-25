package ru.skambet.web.form;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.skambet.service.UserService;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Component
public class RegistrationFormValidator implements Validator {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 64;

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,20}");
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    private final UserService userService;

    public RegistrationFormValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return RegistrationForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RegistrationForm form = (RegistrationForm) target;

        String username = trim(form.getUsername());
        if (username.isEmpty()) {
            errors.rejectValue("username", "required", "Введите логин");
        } else if (!USERNAME.matcher(username).matches()) {
            errors.rejectValue("username", "invalid",
                    "Логин должен содержать от 3 до 20 символов: латинские буквы, цифры или _");
        } else if (userService.isUsernameTaken(username)) {
            errors.rejectValue("username", "taken", "Пользователь с таким логином уже существует");
        }

        String email = trim(form.getEmail());
        if (email.isEmpty()) {
            errors.rejectValue("email", "required", "Введите email");
        } else if (!EMAIL.matcher(email).matches()) {
            errors.rejectValue("email", "invalid", "Введите корректный email");
        } else if (userService.isEmailTaken(email)) {
            errors.rejectValue("email", "taken", "Пользователь с таким email уже существует");
        }

        validateBirthDate(form, errors);

        String password = form.getPassword() == null ? "" : form.getPassword();
        if (password.isEmpty()) {
            errors.rejectValue("password", "required", "Введите пароль");
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            errors.rejectValue("password", "short", "Пароль должен содержать не менее 8 символов");
        } else if (password.length() > MAX_PASSWORD_LENGTH) {
            errors.rejectValue("password", "long", "Пароль должен содержать не более 64 символов");
        } else if (!password.equals(form.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "mismatch", "Пароли не совпадают");
        }
    }

    private void validateBirthDate(RegistrationForm form, Errors errors) {
        if (trim(form.getBirthDate()).isEmpty()) {
            errors.rejectValue("birthDate", "required", "Укажите дату рождения");
            return;
        }
        LocalDate birthDate = form.parsedBirthDate();
        if (birthDate == null) {
            errors.rejectValue("birthDate", "format", "Укажите дату рождения в формате ДД.ММ.ГГГГ");
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
