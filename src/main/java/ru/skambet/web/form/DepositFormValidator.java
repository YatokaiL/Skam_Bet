package ru.skambet.web.form;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.skambet.service.DepositService;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DepositFormValidator implements Validator {

    private static final Pattern CARD_NUMBER = Pattern.compile("\\d{16,19}");
    private static final Pattern EXPIRY = Pattern.compile("(0[1-9]|1[0-2])/(\\d{2})");
    private static final Pattern CVV = Pattern.compile("\\d{3}");

    @Override
    public boolean supports(Class<?> clazz) {
        return DepositForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        DepositForm form = (DepositForm) target;

        if (Amounts.isBlank(form.getAmount())) {
            errors.rejectValue("amount", "required", "Введите сумму пополнения");
        } else {
            BigDecimal amount = Amounts.parse(form.getAmount());
            if (amount == null) {
                errors.rejectValue("amount", "invalid", "Введите корректную сумму");
            } else if (amount.compareTo(DepositService.MIN_AMOUNT) < 0) {
                errors.rejectValue("amount", "min",
                        "Минимальная сумма пополнения — " + DepositService.MIN_AMOUNT.toPlainString() + " ₽");
            } else if (amount.compareTo(DepositService.MAX_AMOUNT) > 0) {
                errors.rejectValue("amount", "max",
                        "Максимальная сумма пополнения — " + DepositService.MAX_AMOUNT.toPlainString() + " ₽");
            }
        }

        String cardNumber = form.getCardNumber() == null ? "" : form.getCardNumber().replaceAll("[\\s-]", "");
        if (cardNumber.isEmpty()) {
            errors.rejectValue("cardNumber", "required", "Введите номер карты");
        } else if (!CARD_NUMBER.matcher(cardNumber).matches() || !passesLuhn(cardNumber)) {
            errors.rejectValue("cardNumber", "invalid", "Неверный номер карты");
        }

        String expiry = form.getCardExpiry() == null ? "" : form.getCardExpiry().trim();
        Matcher expiryMatcher = EXPIRY.matcher(expiry);
        if (expiry.isEmpty()) {
            errors.rejectValue("cardExpiry", "required", "Укажите срок действия карты");
        } else if (!expiryMatcher.matches()) {
            errors.rejectValue("cardExpiry", "format", "Укажите срок действия в формате ММ/ГГ");
        } else {
            int month = Integer.parseInt(expiryMatcher.group(1));
            int year = 2000 + Integer.parseInt(expiryMatcher.group(2));
            if (YearMonth.of(year, month).isBefore(YearMonth.now())) {
                errors.rejectValue("cardExpiry", "expired", "Срок действия карты истёк");
            }
        }

        String cvv = form.getCardCvv() == null ? "" : form.getCardCvv().trim();
        if (!CVV.matcher(cvv).matches()) {
            errors.rejectValue("cardCvv", "invalid", "CVV-код должен состоять из 3 цифр");
        }
    }

    static boolean passesLuhn(String digits) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }
}
