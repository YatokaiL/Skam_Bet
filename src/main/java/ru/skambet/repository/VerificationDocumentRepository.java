package ru.skambet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skambet.domain.VerificationDocument;

public interface VerificationDocumentRepository extends JpaRepository<VerificationDocument, Long> {
}
