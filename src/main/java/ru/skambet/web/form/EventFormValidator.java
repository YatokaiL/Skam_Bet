package ru.skambet.web.form;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ru.skambet.domain.Sport;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Feature 16: checks of a new event and of new odds.
 */
@Component
public class EventFormValidator {

    public static final BigDecimal MIN_ODDS = BigDecimal.ONE;
    public static final BigDecimal MAX_ODDS = new BigDecimal("100");

    public void validateEvent(EventForm form, Errors errors) {
        String home = trim(form.getHomeTeam());
        String away = trim(form.getAwayTeam());
        if (home.isEmpty()) {
            errors.rejectValue("homeTeam", "required", "Укажите команду хозяев");
        }
        if (away.isEmpty()) {
            errors.rejectValue("awayTeam", "required", "Укажите команду гостей");
        } else if (away.equalsIgnoreCase(home)) {
            errors.rejectValue("awayTeam", "same", "Команды должны быть разными");
        }

        if (trim(form.getStartTime()).isEmpty()) {
            errors.rejectValue("startTime", "required", "Укажите время начала");
        } else {
            LocalDateTime start = form.parsedStartTime();
            if (start == null) {
                errors.rejectValue("startTime", "format", "Укажите время начала в формате ДД.ММ.ГГГГ ЧЧ:ММ");
            } else if (!start.isAfter(LocalDateTime.now())) {
                errors.rejectValue("startTime", "past", "Время начала должно быть в будущем");
            }
        }

        if (form.getSport() == null) {
            errors.rejectValue("sport", "required", "Выберите вид спорта");
            return;
        }
        validateOdds(form, form.getSport(), errors);
    }

    /**
     * X is required where a draw is possible and must be empty where it is not.
     */
    public void validateOdds(OddsForm form, Sport sport, Errors errors) {
        checkOdds(form.getOddsHome(), "oddsHome", errors);
        if (sport.isDrawPossible()) {
            if (trim(form.getOddsDraw()).isEmpty()) {
                errors.rejectValue("oddsDraw", "required", "Укажите коэффициент на ничью");
            } else {
                checkOdds(form.getOddsDraw(), "oddsDraw", errors);
            }
        } else if (!trim(form.getOddsDraw()).isEmpty()) {
            errors.rejectValue("oddsDraw", "noDraw", "В этом виде спорта не бывает ничьей");
        }
        checkOdds(form.getOddsAway(), "oddsAway", errors);
    }

    public static BigDecimal parse(String odds) {
        return odds == null || odds.isBlank() ? null : Amounts.parse(odds);
    }

    private static void checkOdds(String value, String field, Errors errors) {
        if (trim(value).isEmpty()) {
            errors.rejectValue(field, "required", "Укажите коэффициент");
            return;
        }
        BigDecimal odds = Amounts.parse(value);
        if (odds == null) {
            errors.rejectValue(field, "invalid", "Коэффициент — число, например 1.85");
        } else if (odds.compareTo(MIN_ODDS) <= 0) {
            errors.rejectValue(field, "min", "Коэффициент должен быть больше 1.00");
        } else if (odds.compareTo(MAX_ODDS) > 0) {
            errors.rejectValue(field, "max", "Коэффициент не может быть больше 100.00");
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
