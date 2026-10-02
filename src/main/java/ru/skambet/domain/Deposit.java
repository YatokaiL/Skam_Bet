package ru.skambet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposits")
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // only last 4 digits are kept, e.g. "**** 1111"
    @Column(nullable = false, length = 9)
    private String cardMask;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Deposit() {
    }

    public Deposit(User user, BigDecimal amount, String cardMask) {
        this.user = user;
        this.amount = amount;
        this.cardMask = cardMask;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCardMask() {
        return cardMask;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
