package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.skambet.domain.OperationType;
import ru.skambet.service.OperationService;

import java.security.Principal;

/**
 * Feature 15: history of operations on the balance.
 */
@Controller
public class OperationsController {

    private final OperationService operationService;

    public OperationsController(OperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("/operations")
    public String operations(@RequestParam(required = false) OperationType type, Principal principal, Model model) {
        model.addAttribute("operations", operationService.getHistory(principal.getName(), type));
        model.addAttribute("types", OperationType.values());
        model.addAttribute("selectedType", type);
        return "operations";
    }
}
