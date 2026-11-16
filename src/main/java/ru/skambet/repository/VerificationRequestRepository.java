package ru.skambet.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.skambet.domain.VerificationRequest;
import ru.skambet.domain.VerificationStatus;

import java.util.List;
import java.util.Optional;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {

    Optional<VerificationRequest> findFirstByUserUsernameOrderByCreatedAtDescIdDesc(String username);

    List<VerificationRequest> findByStatusOrderByCreatedAtAscIdAsc(VerificationStatus status);

    boolean existsByUserIdAndStatus(Long userId, VerificationStatus status);

    // two administrators must not review the same request at once
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from VerificationRequest r where r.id = :id")
    Optional<VerificationRequest> findForUpdate(@Param("id") Long id);
}
