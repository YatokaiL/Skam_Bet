package ru.skambet.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.skambet.domain.User;

import java.math.BigDecimal;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /**
     * select ... for update - balance must not be changed by two requests at the same time
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.username = :username")
    Optional<User> findForUpdate(@Param("username") String username);

    /**
     * Atomic update in the db, does not depend on the user object loaded earlier in the transaction.
     */
    @Modifying
    @Query("update User u set u.balance = u.balance + :amount where u.id = :id")
    void addToBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}
