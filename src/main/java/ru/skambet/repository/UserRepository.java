package ru.skambet.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.skambet.domain.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /**
     * select ... for update - balance must not be changed by two requests at the same time
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.username = :username")
    Optional<User> findForUpdate(@Param("username") String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    /**
     * Feature 20: search in the admin panel by a part of the login or email.
     * The pattern must have %, _ and ! escaped with !, see BlacklistService.
     */
    @Query("select u from User u where lower(u.username) like lower(:pattern) escape '!' "
            + "or lower(u.email) like lower(:pattern) escape '!' order by u.username")
    List<User> search(@Param("pattern") String pattern);

    List<User> findAllByOrderByUsernameAsc();

    /**
     * Atomic update in the db, does not depend on the user object loaded earlier in the transaction.
     */
    @Modifying
    @Query("update User u set u.balance = u.balance + :amount where u.id = :id")
    void addToBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * Current balance right from the db, bypassing the user object of the transaction (feature 15).
     */
    @Query(value = "select balance from users where id = :id", nativeQuery = true)
    BigDecimal balanceOf(@Param("id") Long id);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}
