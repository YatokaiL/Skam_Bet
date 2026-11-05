package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.skambet.domain.Event;
import ru.skambet.domain.Outcome;
import ru.skambet.service.SettlementRejectedException;
import ru.skambet.service.SettlementService;

/**
 * Feature 6: only for administrators, see SecurityConfig.
 */
@Controller
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping("/admin/events")
    public String settlementPage(Model model) {
        model.addAttribute("toSettle", settlementService.getEventsToSettle());
        model.addAttribute("settled", settlementService.getSettledEvents());
        return "admin/events";
    }

    @PostMapping("/admin/events/{id}/settle")
    public String settle(@PathVariable Long id, @RequestParam(required = false) Outcome result,
                         RedirectAttributes redirect) {
        try {
            Event event = settlementService.settle(id, result);
            redirect.addFlashAttribute("success",
                    "Событие «" + event.getTitle() + "» рассчитано, результат: " + event.getResult().getLabel());
        } catch (SettlementRejectedException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/events";
    }
}
