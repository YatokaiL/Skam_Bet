package ru.skambet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.skambet.domain.Bet;
import ru.skambet.domain.BetStatus;

import java.util.List;
import java.util.Optional;

public interface BetRepository extends JpaRepository<Bet, Long> {

    List<Bet> findByUserUsernameOrderByPlacedAtDescIdDesc(String username);

    List<Bet> findByEventIdAndStatus(Long eventId, BetStatus status);

    long countByUserUsername(String username);

    // the event of a bet without loading the bet itself (it is read after the locks, see BetService)
    @Query("select b.event.id from Bet b where b.id = :id")
    Optional<Long> findEventId(@Param("id") Long id);
}
