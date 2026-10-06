package ru.skambet.web.form;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.skambet.service.BetService;

import java.math.BigDecimal;

@Component
public class BetFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return BetForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        BetForm form = (BetForm) target;

        if (Amounts.isBlank(form.getAmount())) {
            errors.rejectValue("amount", "required", "Введите сумму ставки");
            return;
        }
        BigDecimal amount = Amounts.parse(form.getAmount());
        if (amount == null) {
            errors.rejectValue("amount", "invalid", "Введите корректную сумму");
        } else if (amount.compareTo(BetService.MIN_AMOUNT) < 0) {
            errors.rejectValue("amount", "min",
                    "Минимальная сумма ставки — " + BetService.MIN_AMOUNT.toPlainString() + " ₽");
        } else if (amount.compareTo(BetService.MAX_AMOUNT) > 0) {
            errors.rejectValue("amount", "max",
                    "Максимальная сумма ставки — " + BetService.MAX_AMOUNT.toPlainString() + " ₽");
        }
    }
}
