package ru.skambet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skambet.domain.AccountOperation;
import ru.skambet.domain.OperationType;

import java.util.List;

public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {

    List<AccountOperation> findByUserUsernameOrderByCreatedAtDescIdDesc(String username);

    List<AccountOperation> findByUserUsernameAndTypeOrderByCreatedAtDescIdDesc(String username, OperationType type);
}
