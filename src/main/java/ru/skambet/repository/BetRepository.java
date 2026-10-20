package ru.skambet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skambet.domain.Bet;
import ru.skambet.domain.BetStatus;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {

    List<Bet> findByUserUsernameOrderByPlacedAtDescIdDesc(String username);

    List<Bet> findByEventIdAndStatus(Long eventId, BetStatus status);
}
