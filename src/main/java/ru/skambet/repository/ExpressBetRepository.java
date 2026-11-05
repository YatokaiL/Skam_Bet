package ru.skambet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.skambet.domain.BetStatus;
import ru.skambet.domain.ExpressBet;

import java.util.List;

public interface ExpressBetRepository extends JpaRepository<ExpressBet, Long> {

    List<ExpressBet> findByUserUsernameOrderByPlacedAtDescIdDesc(String username);

    @Query("select distinct x from ExpressBet x join x.legs l where l.event.id = :eventId and x.status = :status")
    List<ExpressBet> findByEventAndStatus(@Param("eventId") Long eventId, @Param("status") BetStatus status);
}
