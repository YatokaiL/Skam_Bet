package ru.skambet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "bets")
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Outcome outcome;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal odds;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal potentialWin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BetStatus status = BetStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime placedAt;

    protected Bet() {
    }

    public Bet(User user, Event event, Outcome outcome, BigDecimal odds, BigDecimal amount) {
        this.user = user;
        this.event = event;
        this.outcome = outcome;
        this.odds = odds;
        this.amount = amount;
        this.potentialWin = amount.multiply(odds).setScale(2, RoundingMode.HALF_UP);
        this.placedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Event getEvent() {
        return event;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public BigDecimal getOdds() {
        return odds;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPotentialWin() {
        return potentialWin;
    }

    public BetStatus getStatus() {
        return status;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }
}
