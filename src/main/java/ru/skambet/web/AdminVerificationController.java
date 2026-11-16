package ru.skambet.web;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.skambet.domain.VerificationRequest;
import ru.skambet.service.VerificationException;
import ru.skambet.service.VerificationService;

import java.nio.charset.StandardCharsets;

/**
 * Feature 19: the administrator reviews documents. Only for ADMIN, see SecurityConfig.
 */
@Controller
public class AdminVerificationController {

    private static final int MAX_REASON_LENGTH = 200;

    private final VerificationService verificationService;

    public AdminVerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/admin/verification")
    public String requests(Model model) {
        model.addAttribute("requests", verificationService.getPendingRequests());
        return "admin/verification";
    }

    @GetMapping("/admin/verification/{id}/document")
    public ResponseEntity<byte[]> document(@PathVariable Long id) {
        VerificationService.Document document = verificationService.getDocument(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(document.fileName(), StandardCharsets.UTF_8).build().toString())
                .body(document.content());
    }

    @PostMapping("/admin/verification/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            VerificationRequest request = verificationService.approve(id);
            redirect.addFlashAttribute("success",
                    "Верификация пользователя " + request.getUser().getUsername() + " подтверждена");
        } catch (VerificationException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/verification";
    }

    @PostMapping("/admin/verification/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam(required = false) String reason,
                         RedirectAttributes redirect) {
        String text = reason == null ? "" : reason.trim();
        if (text.isEmpty() || text.length() > MAX_REASON_LENGTH) {
            redirect.addFlashAttribute("error", text.isEmpty() ? "Укажите причину отклонения"
                    : "Причина — не длиннее " + MAX_REASON_LENGTH + " символов");
            return "redirect:/admin/verification";
        }
        try {
            VerificationRequest request = verificationService.reject(id, text);
            redirect.addFlashAttribute("success",
                    "Документ пользователя " + request.getUser().getUsername() + " отклонён");
        } catch (VerificationException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/verification";
    }
}
