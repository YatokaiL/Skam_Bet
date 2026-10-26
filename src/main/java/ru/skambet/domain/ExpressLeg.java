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

/**
 * One event of an express bet with the odds fixed at the moment of the bet.
 */
@Entity
@Table(name = "express_legs")
public class ExpressLeg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "express_id")
    private ExpressBet express;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Outcome outcome;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal odds;

    protected ExpressLeg() {
    }

    ExpressLeg(ExpressBet express, Event event, Outcome outcome, BigDecimal odds) {
        this.express = express;
        this.event = event;
        this.outcome = outcome;
        this.odds = odds;
    }

    public Long getId() {
        return id;
    }

    public ExpressBet getExpress() {
        return express;
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
}
