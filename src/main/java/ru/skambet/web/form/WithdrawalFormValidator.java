package ru.skambet.web.form;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.skambet.service.WithdrawalService;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@Component
public class WithdrawalFormValidator implements Validator {

    private static final Pattern CARD_NUMBER = Pattern.compile("\\d{16,19}");

    @Override
    public boolean supports(Class<?> clazz) {
        return WithdrawalForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        WithdrawalForm form = (WithdrawalForm) target;

        if (Amounts.isBlank(form.getAmount())) {
            errors.rejectValue("amount", "required", "Введите сумму вывода");
        } else {
            BigDecimal amount = Amounts.parse(form.getAmount());
            if (amount == null) {
                errors.rejectValue("amount", "invalid", "Введите корректную сумму");
            } else if (amount.compareTo(WithdrawalService.MIN_AMOUNT) < 0) {
                errors.rejectValue("amount", "min",
                        "Минимальная сумма вывода — " + WithdrawalService.MIN_AMOUNT.toPlainString() + " ₽");
            } else if (amount.compareTo(WithdrawalService.MAX_AMOUNT) > 0) {
                errors.rejectValue("amount", "max",
                        "Максимальная сумма вывода — " + WithdrawalService.MAX_AMOUNT.toPlainString() + " ₽");
            }
        }

        String cardNumber = form.getCardNumber() == null ? "" : form.getCardNumber().replaceAll("[\\s-]", "");
        if (cardNumber.isEmpty()) {
            errors.rejectValue("cardNumber", "required", "Введите номер карты");
        } else if (!CARD_NUMBER.matcher(cardNumber).matches() || !DepositFormValidator.passesLuhn(cardNumber)) {
            errors.rejectValue("cardNumber", "invalid", "Неверный номер карты");
        }
    }
}
