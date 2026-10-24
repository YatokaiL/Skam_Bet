package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.skambet.domain.Withdrawal;
import ru.skambet.service.InsufficientFundsException;
import ru.skambet.service.WithdrawalService;
import ru.skambet.web.form.Amounts;
import ru.skambet.web.form.WithdrawalForm;
import ru.skambet.web.form.WithdrawalFormValidator;

import java.security.Principal;

/**
 * Feature 7.
 */
@Controller
public class WithdrawalController {

    private final WithdrawalService withdrawalService;
    private final WithdrawalFormValidator withdrawalValidator;
    private final Formats formats;

    public WithdrawalController(WithdrawalService withdrawalService, WithdrawalFormValidator withdrawalValidator,
                                Formats formats) {
        this.withdrawalService = withdrawalService;
        this.withdrawalValidator = withdrawalValidator;
        this.formats = formats;
    }

    @GetMapping("/withdraw")
    public String withdrawPage(Principal principal, Model model) {
        model.addAttribute("form", new WithdrawalForm());
        model.addAttribute("withdrawals", withdrawalService.getHistory(principal.getName()));
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@ModelAttribute("form") WithdrawalForm form, BindingResult result,
                           Principal principal, Model model, RedirectAttributes redirect) {
        withdrawalValidator.validate(form, result);
        if (!result.hasErrors()) {
            try {
                Withdrawal w = withdrawalService.withdraw(principal.getName(), Amounts.parse(form.getAmount()),
                        form.getCardNumber());
                redirect.addFlashAttribute("success",
                        "Выведено " + formats.money(w.getAmount()) + " на карту " + w.getCardMask());
                return "redirect:/withdraw";
            } catch (InsufficientFundsException e) {
                result.rejectValue("amount", "insufficient", e.getMessage());
            }
        }
        model.addAttribute("withdrawals", withdrawalService.getHistory(principal.getName()));
        return "withdraw";
    }
}
