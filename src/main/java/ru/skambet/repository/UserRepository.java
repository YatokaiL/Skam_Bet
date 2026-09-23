package ru.skambet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skambet.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}
