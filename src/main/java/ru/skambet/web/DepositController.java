package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.skambet.service.DepositService;
import ru.skambet.web.form.Amounts;
import ru.skambet.web.form.DepositForm;
import ru.skambet.web.form.DepositFormValidator;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
public class DepositController {

    private final DepositService depositService;
    private final DepositFormValidator depositValidator;
    private final Formats formats;

    public DepositController(DepositService depositService, DepositFormValidator depositValidator, Formats formats) {
        this.depositService = depositService;
        this.depositValidator = depositValidator;
        this.formats = formats;
    }

    @GetMapping("/deposit")
    public String depositPage(Principal principal, Model model) {
        model.addAttribute("form", new DepositForm());
        return "deposit";
    }

    @PostMapping("/deposit")
    public String deposit(@ModelAttribute("form") DepositForm form, BindingResult result,
                          Principal principal, Model model, RedirectAttributes redirect) {
        depositValidator.validate(form, result);
        if (result.hasErrors()) {
            return "deposit";
        }
        BigDecimal amount = Amounts.parse(form.getAmount());
        depositService.deposit(principal.getName(), amount, form.getCardNumber());
        redirect.addFlashAttribute("success", "Баланс пополнен на " + formats.money(amount));
        return "redirect:/deposit";
    }
}
