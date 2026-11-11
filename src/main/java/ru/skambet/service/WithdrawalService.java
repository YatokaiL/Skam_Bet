package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.OperationType;
import ru.skambet.domain.User;
import ru.skambet.domain.Withdrawal;
import ru.skambet.repository.UserRepository;
import ru.skambet.repository.WithdrawalRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Feature 7: withdrawal of money from the balance to a bank card.
 * Payment gateway is not real, as for deposits.
 */
@Service
public class WithdrawalService {

    public static final BigDecimal MIN_AMOUNT = new BigDecimal("100");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");

    private final UserRepository userRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final OperationService operationService;

    public WithdrawalService(UserRepository userRepository, WithdrawalRepository withdrawalRepository,
                             OperationService operationService) {
        this.userRepository = userRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.operationService = operationService;
    }

    @Transactional
    public Withdrawal withdraw(String username, BigDecimal amount, String cardNumber) {
        User user = userRepository.findForUpdate(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (user.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        user.withdraw(amount);
        Withdrawal withdrawal = withdrawalRepository.save(
                new Withdrawal(user, amount, DepositService.mask(cardNumber)));
        operationService.record(user, OperationType.WITHDRAWAL, amount.negate(),
                "Вывод на карту " + withdrawal.getCardMask());
        return withdrawal;
    }

    @Transactional(readOnly = true)
    public List<Withdrawal> getHistory(String username) {
        return withdrawalRepository.findByUserUsernameOrderByCreatedAtDescIdDesc(username);
    }
}
