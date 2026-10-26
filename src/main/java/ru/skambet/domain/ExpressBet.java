package ru.skambet.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Feature 8: several events in one bet. Odds are multiplied,
 * the express wins only if every event is guessed.
 */
@Entity
@Table(name = "express_bets")
public class ExpressBet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "express", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ExpressLeg> legs = new ArrayList<>();

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal odds;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal potentialWin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BetStatus status = BetStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime placedAt;

    protected ExpressBet() {
    }

    public ExpressBet(User user, BigDecimal amount) {
        this.user = user;
        this.amount = amount;
        this.odds = BigDecimal.ONE;
        this.potentialWin = amount;
        this.placedAt = LocalDateTime.now();
    }

    public void addLeg(Event event, Outcome outcome, BigDecimal legOdds) {
        legs.add(new ExpressLeg(this, event, outcome, legOdds));
        odds = totalOdds(legs.stream().map(ExpressLeg::getOdds).toList());
        potentialWin = amount.multiply(odds).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Product of the odds rounded to 2 digits, the same value is shown in the coupon.
     */
    public static BigDecimal totalOdds(List<BigDecimal> legOdds) {
        BigDecimal result = BigDecimal.ONE;
        for (BigDecimal o : legOdds) {
            result = result.multiply(o);
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Called after one of the events got its result.
     * Any wrong leg - LOST at once; all legs guessed and all events finished - WON.
     */
    public void settle() {
        if (status != BetStatus.PENDING) {
            return;
        }
        boolean allSettled = true;
        for (ExpressLeg leg : legs) {
            Outcome result = leg.getEvent().getResult();
            if (result == null) {
                allSettled = false;
            } else if (result != leg.getOutcome()) {
                status = BetStatus.LOST;
                return;
            }
        }
        if (allSettled) {
            status = BetStatus.WON;
        }
    }

    public String getDescription() {
        return legs.stream()
                .map(l -> l.getEvent().getTitle() + " (" + l.getOutcome().getLabel() + ")")
                .collect(Collectors.joining(", "));
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public List<ExpressLeg> getLegs() {
        return legs;
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
