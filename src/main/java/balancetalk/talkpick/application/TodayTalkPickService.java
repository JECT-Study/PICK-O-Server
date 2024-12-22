package balancetalk.talkpick.application;

import static balancetalk.talkpick.dto.TodayTalkPickDto.TodayTalkPickResponse;

import balancetalk.talkpick.domain.TalkPick;
import balancetalk.talkpick.domain.TodayTalkPick;
import balancetalk.talkpick.domain.repository.TalkPickRepository;
import balancetalk.talkpick.domain.repository.TodayTalkPickRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodayTalkPickService {

    private final TalkPickRepository talkPickRepository;
    private final TodayTalkPickRepository todayTalkPickRepository;

    @Value("${pick-o.candidate-today-talk-pick-count}")
    private int candidateTodayTalkPickCount;

    @Value("${pick-o.today-talk-pick-count}")
    private int todayTalkPickCount;

    @Scheduled(cron = "0 0 0 * * ?")
    public void createTodayTalkPick() {
        List<TalkPick> candidateTodayTalkPicks = getCandidateTodayTalkPicks();
        Collections.shuffle(candidateTodayTalkPicks);
        List<TodayTalkPick> todayTalkPicks = candidateTodayTalkPicks.subList(0, todayTalkPickCount)
                .stream()
                .map(TalkPick::toTodayTalkPick)
                .toList();
        todayTalkPickRepository.saveAll(todayTalkPicks);
    }

    private List<TalkPick> getCandidateTodayTalkPicks() {
        List<TalkPick> yesterdayTalkPicks = getYesterdaySelectedTalkPicks();
        return talkPickRepository.findCandidateTodayTalkPicks(candidateTodayTalkPickCount, yesterdayTalkPicks)
                .stream()
                .filter(talkPick -> !yesterdayTalkPicks.contains(talkPick))
                .collect(Collectors.toList());
    }

    private List<TalkPick> getYesterdaySelectedTalkPicks() {
        return todayTalkPickRepository.findByPickDate(LocalDate.now().minusDays(1L))
                .stream()
                .map(TodayTalkPick::getTalkPick)
                .toList();
    }

    public TodayTalkPickResponse findTodayTalkPick() {
        return null;
    }
}
