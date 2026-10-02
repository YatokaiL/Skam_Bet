package ru.skambet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skambet.domain.Deposit;

public interface DepositRepository extends JpaRepository<Deposit, Long> {
}
