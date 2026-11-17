package ru.skambet.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;
import ru.skambet.service.BlacklistService;

import java.security.Principal;
import java.util.Optional;

/**
 * Feature 20: a blocked user is logged out at the very next request, even if the session was opened before the block.
 * Logging in works the same way: the password is right, but the first page after it closes the session again,
 * so the reason of the block is shown only to someone who knows the password.
 */
@Component
public class BlockedUserInterceptor implements HandlerInterceptor {

    private final BlacklistService blacklistService;

    public BlockedUserInterceptor(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return true;
        }
        Optional<String> message = blacklistService.blockMessage(principal.getName());
        if (message.isEmpty()) {
            return true;
        }
        new SecurityContextLogoutHandler().logout(request, response,
                SecurityContextHolder.getContext().getAuthentication());

        // the message goes to the login page in a new session
        String loginUrl = request.getContextPath() + "/login";
        RequestContextUtils.getOutputFlashMap(request).put("blocked", message.get());
        RequestContextUtils.saveOutputFlashMap(loginUrl, request, response);

        if (isAjax(request)) {
            // cashout.js asks for json - the page reloads itself and gets to the login page
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            response.sendRedirect(loginUrl);
        }
        return false;
    }

    private static boolean isAjax(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE);
    }
}
