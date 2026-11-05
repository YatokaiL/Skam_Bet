package ru.skambet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skambet.domain.Event;
import ru.skambet.domain.Sport;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime time);

    List<Event> findBySportAndStartTimeAfterOrderByStartTimeAsc(Sport sport, LocalDateTime time);

    // feature 6: started events waiting for the result
    List<Event> findByStartTimeLessThanEqualAndResultIsNullOrderByStartTimeAsc(LocalDateTime time);

    List<Event> findByResultIsNotNullOrderBySettledAtDescIdDesc();
}
