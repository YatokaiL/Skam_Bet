package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.AccountOperation;
import ru.skambet.domain.OperationType;
import ru.skambet.domain.User;
import ru.skambet.repository.AccountOperationRepository;
import ru.skambet.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Feature 15: history of operations. Services that change a balance call record(...)
 * in the same transaction, so the history and the balance can't differ.
 */
@Service
public class OperationService {

    private final AccountOperationRepository operationRepository;
    private final UserRepository userRepository;

    public OperationService(AccountOperationRepository operationRepository, UserRepository userRepository) {
        this.operationRepository = operationRepository;
        this.userRepository = userRepository;
    }

    /**
     * @param user   locked user whose balance is already changed
     * @param amount positive - to the balance, negative - from it
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(User user, OperationType type, BigDecimal amount, String description) {
        operationRepository.save(new AccountOperation(user, type, amount, user.getBalance(), description));
    }

    /**
     * For balances changed by an update query (settlement): the balance is read again from the db.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordAfterUpdate(User user, OperationType type, BigDecimal amount, String description) {
        BigDecimal balance = userRepository.balanceOf(user.getId());
        operationRepository.save(new AccountOperation(user, type, amount, balance, description));
    }

    @Transactional(readOnly = true)
    public List<AccountOperation> getHistory(String username, OperationType type) {
        if (type == null) {
            return operationRepository.findByUserUsernameOrderByCreatedAtDescIdDesc(username);
        }
        return operationRepository.findByUserUsernameAndTypeOrderByCreatedAtDescIdDesc(username, type);
    }
}
