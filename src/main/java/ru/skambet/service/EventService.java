package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.Event;
import ru.skambet.domain.Sport;
import ru.skambet.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

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

    @Transactional(readOnly = true)
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }
}
