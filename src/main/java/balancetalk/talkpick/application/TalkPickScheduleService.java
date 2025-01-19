package balancetalk.talkpick.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TalkPickScheduleService {

    private final TalkPickSummaryService talkPickSummaryService;
    private final TodayTalkPickService todayTalkPickService;

//    @Scheduled(cron = "0 30 00 * * ?")
    public void retryFailedSummaries() {
        talkPickSummaryService.summarizeFailedTalkPick();
    }

    @Scheduled(cron = "0 00 00 * * ?")
    public void updateTodayTalkPick() {
        todayTalkPickService.updateTodayTalkPick();
    }
}
