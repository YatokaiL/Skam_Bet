package ru.skambet.web;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import ru.skambet.domain.Outcome;
import ru.skambet.service.ExpressService;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature 8: events chosen for the express, kept in the http session until the bet is placed.
 * One outcome per event, choosing another outcome of the same event replaces it.
 */
@Component
@SessionScope
public class ExpressCoupon implements Serializable {

    // event id -> outcome, in the order they were added
    private final Map<Long, Outcome> selections = new LinkedHashMap<>();

    /**
     * @return false if the coupon is full
     */
    public synchronized boolean add(Long eventId, Outcome outcome) {
        if (!selections.containsKey(eventId) && selections.size() >= ExpressService.MAX_EVENTS) {
            return false;
        }
        selections.put(eventId, outcome);
        return true;
    }

    public synchronized void remove(Long eventId) {
        selections.remove(eventId);
    }

    public synchronized void clear() {
        selections.clear();
    }

    public synchronized boolean isSelected(Long eventId, Outcome outcome) {
        return outcome == selections.get(eventId);
    }

    public synchronized List<ExpressService.Selection> getSelections() {
        return selections.entrySet().stream()
                .map(e -> new ExpressService.Selection(e.getKey(), e.getValue()))
                .toList();
    }
}
