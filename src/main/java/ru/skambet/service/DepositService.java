package ru.skambet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skambet.domain.Deposit;
import ru.skambet.domain.User;
import ru.skambet.repository.DepositRepository;
import ru.skambet.repository.UserRepository;

import java.math.BigDecimal;

@Service
public class DepositService {

    public static final BigDecimal MIN_AMOUNT = new BigDecimal("100");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");

    private final UserRepository userRepository;
    private final DepositRepository depositRepository;

    public DepositService(UserRepository userRepository, DepositRepository depositRepository) {
        this.userRepository = userRepository;
        this.depositRepository = depositRepository;
    }

    /**
     * Payment gateway is not real here - we just trust the validated card data
     * and keep only the masked number.
     */
    @Transactional
    public Deposit deposit(String username, BigDecimal amount, String cardNumber) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        user.deposit(amount);
        return depositRepository.save(new Deposit(user, amount, mask(cardNumber)));
    }

    private static String mask(String cardNumber) {
        String digits = cardNumber.replaceAll("\\D", "");
        return "**** " + digits.substring(digits.length() - 4);
    }
}
