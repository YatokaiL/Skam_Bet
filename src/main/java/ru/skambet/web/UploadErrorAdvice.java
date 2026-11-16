package ru.skambet.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Feature 19: a file much bigger than allowed is rejected while the request is read,
 * before any controller is chosen - so it is handled here.
 */
@ControllerAdvice
public class UploadErrorAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tooBig(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "Размер файла — не больше 5 МБ");
        return "redirect:/verification";
    }
}
