package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.Event;
import ru.skambet.domain.Sport;
import ru.skambet.repository.EventRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Feature 16: the administrator adds events to the line and changes their odds.
 * Bets that are already placed keep the odds they were placed with.
 */
@Service
public class LineService {

    private final EventRepository eventRepository;

    public LineService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Event addEvent(Sport sport, String homeTeam, String awayTeam, LocalDateTime startTime,
                          BigDecimal oddsHome, BigDecimal oddsDraw, BigDecimal oddsAway) {
        return eventRepository.save(new Event(sport, homeTeam, awayTeam, startTime, oddsHome, oddsDraw, oddsAway));
    }

    @Transactional
    public Event changeOdds(Long eventId, BigDecimal oddsHome, BigDecimal oddsDraw, BigDecimal oddsAway) {
        Event event = eventRepository.findForUpdate(eventId)
                .orElseThrow(() -> new LineException("Событие не найдено"));
        if (event.isStarted()) {
            throw new LineException("Событие уже началось, коэффициенты изменить нельзя");
        }
        event.changeOdds(oddsHome, oddsDraw, oddsAway);
        return event;
    }
}
