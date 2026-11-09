package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.Event;
import ru.skambet.domain.Sport;
import ru.skambet.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class EventService {

    public static final int MIN_QUERY_LENGTH = 2;
    public static final int MAX_QUERY_LENGTH = 50;

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Events that have not started yet, nearest first. If sport is null - all sports.
     */
    @Transactional(readOnly = true)
    public List<Event> getLine(Sport sport) {
        LocalDateTime now = LocalDateTime.now();
        if (sport == null) {
            return eventRepository.findByStartTimeAfterOrderByStartTimeAsc(now);
        }
        return eventRepository.findBySportAndStartTimeAfterOrderByStartTimeAsc(sport, now);
    }

    /**
     * Feature 11: the line filtered by a part of a team name, case and ё/е don't matter.
     * Filtered here and not in sql - lower() of cyrillic depends on the locale of the db.
     */
    @Transactional(readOnly = true)
    public List<Event> getLine(Sport sport, String query) {
        List<Event> line = getLine(sport);
        if (query == null || query.isBlank()) {
            return line;
        }
        String needle = normalize(query);
        return line.stream()
                .filter(e -> normalize(e.getTitle()).contains(needle))
                .toList();
    }

    /**
     * Feature 13: settled events, the last settled first. If sport is null - all sports.
     */
    @Transactional(readOnly = true)
    public List<Event> getResults(Sport sport) {
        if (sport == null) {
            return eventRepository.findByResultIsNotNullOrderBySettledAtDescIdDesc();
        }
        return eventRepository.findBySportAndResultIsNotNullOrderBySettledAtDescIdDesc(sport);
    }

    @Transactional(readOnly = true)
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    private static String normalize(String text) {
        return text.trim().toLowerCase(Locale.ROOT).replace('ё', 'е');
    }
}
