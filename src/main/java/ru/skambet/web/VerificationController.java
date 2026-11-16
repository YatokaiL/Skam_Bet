package ru.skambet.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.skambet.service.VerificationException;
import ru.skambet.service.VerificationService;

import java.io.IOException;
import java.security.Principal;

/**
 * Feature 19: the user sends a document for verification of identity.
 */
@Controller
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/verification")
    public String page(Principal principal, Model model) {
        model.addAttribute("state", verificationService.getState(principal.getName()));
        return "verification";
    }

    @PostMapping("/verification")
    public String submit(@RequestParam(name = "document", required = false) MultipartFile document,
                         Principal principal, RedirectAttributes redirect) throws IOException {
        try {
            byte[] content = document == null ? null : document.getBytes();
            String fileName = document == null ? null : document.getOriginalFilename();
            verificationService.submit(principal.getName(), fileName, content);
            redirect.addFlashAttribute("success", "Документ отправлен на проверку");
        } catch (VerificationException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/verification";
    }
}
