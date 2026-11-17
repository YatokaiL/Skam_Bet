package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.BlockTerm;
import ru.skambet.domain.User;
import ru.skambet.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Feature 20: the administrator blocks a user for a term with a reason.
 * A blocked user can't log in, and an open session is closed at the next request (see BlockedUserInterceptor).
 */
@Service
public class BlacklistService {

    public static final int MAX_REASON_LENGTH = 200;

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final UserRepository userRepository;

    public BlacklistService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> findUsers(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findAllByOrderByUsernameAsc();
        }
        String escaped = query.trim()
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return userRepository.search("%" + escaped + "%");
    }

    @Transactional
    public User block(String adminUsername, Long userId, BlockTerm term, String reason) {
        String text = reason == null ? "" : reason.trim();
        if (text.isEmpty()) {
            throw new BlacklistException("Укажите причину блокировки");
        }
        if (text.length() > MAX_REASON_LENGTH) {
            throw new BlacklistException("Причина — не длиннее " + MAX_REASON_LENGTH + " символов");
        }
        if (term == null) {
            throw new BlacklistException("Выберите срок блокировки");
        }
        User user = lock(userId);
        // the page does not show the form for these users, but a request can be sent by hand
        if (user.getUsername().equals(adminUsername)) {
            throw new BlacklistException("Нельзя заблокировать самого себя");
        }
        if (user.isAdmin()) {
            throw new BlacklistException("Нельзя заблокировать администратора");
        }
        LocalDateTime now = LocalDateTime.now();
        // a new block replaces the old one, e.g. a day is changed to forever
        user.block(now, term.until(now), text);
        return user;
    }

    @Transactional
    public User unblock(Long userId) {
        User user = lock(userId);
        if (!user.isBlocked()) {
            throw new BlacklistException("Пользователь " + user.getUsername() + " не заблокирован");
        }
        user.unblock();
        return user;
    }

    /**
     * The text for the user, if the user is blocked right now.
     */
    @Transactional(readOnly = true)
    public Optional<String> blockMessage(String username) {
        return userRepository.findByUsername(username)
                .filter(User::isBlocked)
                .map(BlacklistService::message);
    }

    public static String term(User user) {
        return user.getBlockedUntil() == null ? "навсегда" : "до " + DATE_TIME.format(user.getBlockedUntil());
    }

    private static String message(User user) {
        return "Ваш аккаунт заблокирован " + term(user) + ". Причина: " + user.getBlockReason();
    }

    private User lock(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BlacklistException("Пользователь не найден"));
    }
}
