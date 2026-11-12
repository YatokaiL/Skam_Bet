package ru.skambet.web.form;

import ru.skambet.domain.Sport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Feature 16: a new event of the line, filled by the administrator.
 */
public class EventForm extends OddsForm {

    public static final DateTimeFormatter START_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private Sport sport = Sport.FOOTBALL;
    private String homeTeam;
    private String awayTeam;
    private String startTime;

    /**
     * @return start time or null if it is empty or has a wrong format
     */
    public LocalDateTime parsedStartTime() {
        if (startTime == null || startTime.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(startTime.trim(), START_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
