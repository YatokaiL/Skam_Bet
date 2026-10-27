package ru.skambet.web.form;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.skambet.service.BetService;

import java.math.BigDecimal;

/**
 * Same limits for the amount as for a single bet.
 */
@Component
public class ExpressFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ExpressForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ExpressForm form = (ExpressForm) target;

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
