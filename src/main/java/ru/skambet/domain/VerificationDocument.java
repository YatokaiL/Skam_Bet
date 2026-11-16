package ru.skambet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Feature 19: the file of a verification request, kept in a separate table.
 */
@Entity
@Table(name = "verification_documents")
public class VerificationDocument {

    @Id
    private Long requestId;

    @Column(nullable = false)
    private byte[] content;

    protected VerificationDocument() {
    }

    public VerificationDocument(Long requestId, byte[] content) {
        this.requestId = requestId;
        this.content = content;
    }

    public Long getRequestId() {
        return requestId;
    }

    public byte[] getContent() {
        return content;
    }
}
