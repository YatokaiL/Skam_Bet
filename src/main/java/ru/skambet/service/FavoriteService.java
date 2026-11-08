package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.Event;
import ru.skambet.domain.Favorite;
import ru.skambet.domain.User;
import ru.skambet.repository.EventRepository;
import ru.skambet.repository.FavoriteRepository;
import ru.skambet.repository.UserRepository;

import java.util.Optional;
import java.util.Set;

/**
 * Feature 12: favorite events of a user.
 */
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository,
                           EventRepository eventRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Adds the event to the favorites or removes it if it is already there.
     *
     * @return true if the event was added
     */
    @Transactional
    public boolean toggle(String username, Long eventId) {
        Optional<Favorite> favorite = favoriteRepository.findByUserUsernameAndEventId(username, eventId);
        if (favorite.isPresent()) {
            favoriteRepository.delete(favorite.get());
            return false;
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        favoriteRepository.save(new Favorite(user, event));
        return true;
    }

    @Transactional(readOnly = true)
    public Set<Long> getFavoriteEventIds(String username) {
        return favoriteRepository.findEventIds(username);
    }
}
