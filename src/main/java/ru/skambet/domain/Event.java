package ru.skambet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sport sport;

    @Column(nullable = false)
    private String homeTeam;

    @Column(nullable = false)
    private String awayTeam;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal oddsHome;

    // null when the sport has no draw (basketball, tennis...)
    @Column(precision = 6, scale = 2)
    private BigDecimal oddsDraw;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal oddsAway;

    // feature 6: filled by the administrator after the match
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Outcome result;

    private LocalDateTime settledAt;

    protected Event() {
    }

    public Event(Sport sport, String homeTeam, String awayTeam, LocalDateTime startTime,
                 BigDecimal oddsHome, BigDecimal oddsDraw, BigDecimal oddsAway) {
        this.sport = sport;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.startTime = startTime;
        this.oddsHome = oddsHome;
        this.oddsDraw = oddsDraw;
        this.oddsAway = oddsAway;
    }

    public String getTitle() {
        return homeTeam + " — " + awayTeam;
    }

    public boolean isStarted() {
        return !startTime.isAfter(LocalDateTime.now());
    }

    public BigDecimal getOdds(Outcome outcome) {
        return switch (outcome) {
            case HOME -> oddsHome;
            case DRAW -> oddsDraw;
            case AWAY -> oddsAway;
        };
    }

    public boolean isSettled() {
        return result != null;
    }

    /**
     * Outcomes that can be the result: draw is impossible when the sport has no draw odds.
     */
    public List<Outcome> getPossibleOutcomes() {
        return Arrays.stream(Outcome.values())
                .filter(o -> getOdds(o) != null)
                .toList();
    }

    public void settle(Outcome result) {
        this.result = result;
        this.settledAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Sport getSport() {
        return sport;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public BigDecimal getOddsHome() {
        return oddsHome;
    }

    public BigDecimal getOddsDraw() {
        return oddsDraw;
    }

    public BigDecimal getOddsAway() {
        return oddsAway;
    }

    public Outcome getResult() {
        return result;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }
}
