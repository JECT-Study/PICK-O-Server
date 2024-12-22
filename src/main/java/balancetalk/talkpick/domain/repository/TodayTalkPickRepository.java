package balancetalk.talkpick.domain.repository;

import balancetalk.talkpick.domain.TodayTalkPick;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodayTalkPickRepository extends JpaRepository<TodayTalkPick, Long> {

    List<TodayTalkPick> findByPickDate(LocalDate pickDate);
}
