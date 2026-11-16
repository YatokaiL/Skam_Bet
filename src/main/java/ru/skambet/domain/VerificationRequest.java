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

import java.time.LocalDateTime;

/**
 * Feature 19: a document sent by a user for verification of identity.
 * The file itself is in VerificationDocument.
 */
@Entity
@Table(name = "verification_requests")
public class VerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private int fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VerificationStatus status = VerificationStatus.PENDING;

    private String rejectReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    protected VerificationRequest() {
    }

    public VerificationRequest(User user, String fileName, String contentType, int fileSize) {
        this.user = user;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return status == VerificationStatus.PENDING;
    }

    public void approve() {
        status = VerificationStatus.APPROVED;
        reviewedAt = LocalDateTime.now();
        user.verify(reviewedAt);
    }

    public void reject(String reason) {
        status = VerificationStatus.REJECTED;
        rejectReason = reason;
        reviewedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public int getFileSize() {
        return fileSize;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
}
