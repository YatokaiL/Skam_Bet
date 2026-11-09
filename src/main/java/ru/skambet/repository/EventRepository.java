package ru.skambet.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.skambet.domain.Event;
import ru.skambet.domain.Sport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime time);

    List<Event> findBySportAndStartTimeAfterOrderByStartTimeAsc(Sport sport, LocalDateTime time);

    // feature 6: started events waiting for the result
    List<Event> findByStartTimeLessThanEqualAndResultIsNullOrderByStartTimeAsc(LocalDateTime time);

    List<Event> findByResultIsNotNullOrderBySettledAtDescIdDesc();

    // feature 13: results of one sport
    List<Event> findBySportAndResultIsNotNullOrderBySettledAtDescIdDesc(Sport sport);

    /**
     * select ... for update - two administrators must not settle the same event at once
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findForUpdate(@Param("id") Long id);
}
