package ru.skambet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.skambet.domain.Favorite;

import java.util.Optional;
import java.util.Set;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserUsernameAndEventId(String username, Long eventId);

    @Query("select f.event.id from Favorite f where f.user.username = :username")
    Set<Long> findEventIds(@Param("username") String username);
}
